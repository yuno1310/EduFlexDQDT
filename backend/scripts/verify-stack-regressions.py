#!/usr/bin/env python3
"""Exercise a local Compose API against PostgreSQL, including security regressions.

Uses disposable e2e-* accounts/courses, removes them in finally, and returns 1
when a behavioral assertion fails. Requires Python 3, Docker, a ready API, and
Compose credentials matching its database. Never prints session tokens.
"""
import argparse
import json
from pathlib import Path
import subprocess
import time
import urllib.request
import urllib.error
import urllib.parse
import uuid
from concurrent.futures import ThreadPoolExecutor
ROOT = Path(__file__).resolve().parents[2]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--api-url', default='http://localhost:8080')
parser.add_argument('--env-file', default=str(ROOT / 'backend/.env'))
parser.add_argument('--compose-file', default=str(ROOT / 'backend/docker-compose.yml'))
parser.add_argument('--project-name', default=None)
parser.add_argument('--report', type=Path, default=Path('/tmp/eduflex-stack-regressions.json'))
args = parser.parse_args()
compose = ['docker', 'compose', '--env-file', args.env_file, '-f', args.compose_file]
if args.project_name:
    compose += ['--project-name', args.project_name]
results = []
s = {'run': str(int(time.time())) + '-' + uuid.uuid4().hex[:8], 'courses': {}, 'password': 'Journey1!Pass'}

def api(method, path, body=None, token=None, form=None):
    headers = {}
    data = None
    if token:
        headers['Authorization'] = 'Bearer ' + token
    if body is not None:
        headers['Content-Type'] = 'application/json'
        data = json.dumps(body).encode()
    if form is not None:
        headers['Content-Type'] = 'application/x-www-form-urlencoded'
        data = urllib.parse.urlencode(form).encode()
    request = urllib.request.Request(args.api_url.rstrip('/') + path, data=data, headers=headers, method=method)
    try:
        response = urllib.request.urlopen(request, timeout=50)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        payload = response.read()
        return (response.status, json.loads(payload) if payload else None)

def db(sql):
    process = subprocess.run(compose + ['exec', '-T', 'eduflex-postgres', 'sh', '-c', 'psql -qtAX -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 -c "$1"', 'sh', sql], check=True, capture_output=True, text=True, timeout=30)
    return process.stdout.strip()

def check(name, expected, actual):
    passed = expected == actual
    results.append(dict(name=name, passed=passed, expected=expected, actual=actual))
    print(('PASS ' if passed else 'FAIL ') + name + ': ' + str(actual), flush=True)
    return passed

def request(name, method, path, expected, body=None, token=None, form=None):
    status, payload = api(method, path, body, token, form)
    check(name, expected, status)
    return (status, payload)

def required(method, path, body=None, token=None):
    status, payload = api(method, path, body, token)
    if status != 200 or payload.get('success') is False:
        raise RuntimeError('Fixture creation failed: ' + method + ' ' + path + ' status ' + str(status))
    return payload

def setup():
    if api('GET', '/readyz')[0] != 200:
        raise RuntimeError('API is not ready')
    for who in ('admin', 'outsider'):
        email = 'e2e-' + who + '-' + s['run'] + '@example.test'
        s[who + 'Email'] = email
        required('POST', '/api/user/register', dict(email=email, password=s['password'], name='E2E Fixture', active=True))
        if who == 'admin':
            db("UPDATE users SET role='admin' WHERE email='" + email + "'")
        s[who] = required('POST', '/api/user/login', dict(email=email, password=s['password']))
        s[who]['id'] = db("SELECT user_id FROM users WHERE email='" + email + "'")
    admin = s['admin']['accessToken']
    for kind, price in (('Paid', 2500), ('Free', 0)):
        title = 'E2E ' + kind + ' ' + s['run']
        required('POST', '/api/course', dict(title=title, learningModel='self-paced', status='active', description='PostgreSQL transactions and concurrency', price=price), admin)
        course = dict(title=title, id=db("SELECT course_id FROM courses WHERE title='" + title + "'"))
        s['courses'][kind] = course
        lesson = required('POST', '/api/admin/lessons', dict(courseID=course['id'], title=kind + ' transactions lesson', contentType='text', content='PostgreSQL transactions preserve consistency. Redis caches data. RabbitMQ queues embeddings.'), admin)
        course['lesson'] = lesson['lessonId']
        course['quiz'] = db("SELECT lesson_id FROM lesson WHERE parent_lesson_id='" + course['lesson'] + "'")
        if kind == 'Paid':
            for question, answer in (('Which database supports row locking?', 'PostgreSQL'), ('Which system caches data?', 'Redis')):
                required('POST', '/api/admin/quizzes', dict(lessonId=course['quiz'], questionText=question, points=10, options=[dict(optionText=answer, isCorrect=True), dict(optionText='Wrong', isCorrect=False)]), admin)
    deadline = time.monotonic() + 90
    course = s['courses']['Paid']
    while time.monotonic() < deadline:
        if db("SELECT count(*) FROM courses WHERE course_id='" + course['id'] + "' AND embedding IS NOT NULL") == '1' and db("SELECT count(*) FROM lesson WHERE lesson_id='" + course['lesson'] + "' AND embedding IS NOT NULL") == '1':
            return
        time.sleep(1)

def cleanup():
    # Generated run suffixes restrict deletion to this runner's disposable fixtures.
    user_ids = "SELECT user_id FROM users WHERE email LIKE 'e2e-%-" + s['run'] + "@example.test'"
    course_ids = "SELECT course_id FROM courses WHERE title LIKE 'E2E % " + s['run'] + "%'"
    users = db(user_ids).splitlines()
    courses = db(course_ids).splitlines()
    db('BEGIN; DELETE FROM transactions WHERE user_id IN (' + user_ids + ') OR course_id IN (' + course_ids + '); DELETE FROM course_reviews WHERE user_id IN (' + user_ids + ') OR course_id IN (' + course_ids + "); DELETE FROM badges WHERE condition_type IN (SELECT 'COURSE_' || course_id FROM courses WHERE course_id IN (" + course_ids + ')); DELETE FROM courses WHERE course_id IN (' + course_ids + '); DELETE FROM users WHERE user_id IN (' + user_ids + '); COMMIT;')
    for who in ('admin', 'outsider', 'apiUser'):
        if who in s:
            api('POST', '/api/auth/logout', dict(refreshToken=s[who]['refreshToken']))
    # Direct SQL cleanup bypasses cache eviction. Remove fixture entries and
    # shared catalogue/search results that may include the deleted courses.
    patterns = ['courseCatalog::*', 'semanticSearch::*']
    patterns += ['courses::' + user for user in users]
    patterns += ['refresh:' + user + ':*' for user in users]
    patterns += ['lessons::' + course for course in courses]
    patterns += ['summary::' + course for course in courses]
    for pattern in patterns:
        subprocess.run(compose + ['exec', '-T', 'eduflex-redis', 'sh', '-c',
            'redis-cli --no-auth-warning -a "$REDIS_PASSWORD" --scan --pattern "$1" '
            '| xargs -r redis-cli --no-auth-warning -a "$REDIS_PASSWORD" DEL >/dev/null',
            'sh', pattern], check=True, capture_output=True, timeout=30)

def run_checks():
    a = s['admin']['accessToken']
    o = s['outsider']['accessToken']
    oid = s['outsider']['id']
    c = s['courses']['Paid']
    free = s['courses']['Free']
    email = f"e2e-api-{s['run']}@example.test"
    s['apiEmail'] = email
    request('Register API test learner', 'POST', '/api/user/register', 200, {'email': email, 'password': s['password'], 'name': 'E2E API', 'active': True})
    _, login = request('Login API learner', 'POST', '/api/user/login', 200, {'email': email, 'password': s['password']})
    t = login['accessToken']
    uid = db(f"SELECT user_id FROM users WHERE email='{email}'")
    s['apiUser'] = dict(login, id=uid)
    request('Ready probe', 'GET', '/readyz', 200)
    request('Anonymous catalogue denied', 'GET', '/api/course', 401)
    request('Learner admin users denied', 'GET', '/api/admin/users', 403, token=t)
    request('Cross-user enrollment read denied', 'GET', f'/api/enrollment/{uid}', 403, token=o)
    request('Cross-user profile write denied', 'PUT', f'/api/user/update-profile/{uid}', 403, {'fullName': 'Blocked'}, o)
    request('Cross-user stats read denied', 'GET', f'/api/users/{uid}/stats', 403, token=o)
    request('Cross-user lesson completion denied', 'POST', '/api/progress/lesson', 403, token=o, form={'userId': uid, 'lessonId': free['lesson']})
    request('Unenrolled AI summary denied', 'GET', f"/api/course/{c['id']}/ai-summary", 403, token=o)
    request('Unenrolled paid lesson contents denied', 'GET', f"/api/lesson?courseID={c['id']}", 403, token=o)
    request('Unenrolled quiz contents denied', 'GET', f"/api/quiz/{c['quiz']}", 403, token=o)
    request('Unenrolled reviews rejected', 'POST', f"/api/course/{c['id']}/reviews", 403, {'rating': 5, 'comment': 'Unenrolled fixture'}, o)
    request('Paid free-registration rejected', 'POST', f"/api/enrollment/{c['id']}/register", 400, {'userId': uid}, t)
    request('Free enrollment', 'POST', f"/api/enrollment/{free['id']}/register", 200, {'userId': uid}, t)
    request('Simulated paid enrollment', 'POST', '/api/payment', 200, {'userId': uid, 'courseId': c['id']}, t)
    request('Repeat simulated paid enrollment', 'POST', '/api/payment', 200, {'userId': uid, 'courseId': c['id']}, t)
    check('Exactly one payment', 1, int(db(f"SELECT count(*) FROM transactions WHERE user_id='{uid}' AND course_id='{c['id']}'")))
    check('Exactly one paid enrollment', 1, int(db(f"SELECT count(*) FROM enrollments WHERE user_id='{uid}' AND course_id='{c['id']}'")))
    request('Invalid review rating rejected', 'POST', f"/api/course/{c['id']}/reviews", 400, {'rating': 6, 'comment': 'Invalid'}, t)
    request('Enrolled review saved', 'POST', f"/api/course/{c['id']}/reviews", 200, {'rating': 4, 'comment': 'API fixture review'}, t)
    request('Enrolled review updated', 'POST', f"/api/course/{c['id']}/reviews", 200, {'rating': 5, 'comment': 'API fixture updated'}, t)
    check('Review update stores one row', 1, int(db(f"SELECT count(*) FROM course_reviews WHERE user_id='{uid}' AND course_id='{c['id']}'")))
    request('Daily quests load', 'GET', f'/api/users/{uid}/daily-quests', 200, token=t)
    xp0 = int(db(f"SELECT xp FROM gamification_stats WHERE user_id='{uid}'"))
    request('Daily checkin', 'POST', f'/api/users/{uid}/daily-checkin', 200, token=t)
    request('Repeat daily checkin', 'POST', f'/api/users/{uid}/daily-checkin', 200, token=t)
    check('Repeated checkin after login adds no XP', 0, int(db(f"SELECT xp FROM gamification_stats WHERE user_id='{uid}'")) - xp0)
    xp0 = int(db(f"SELECT xp FROM gamification_stats WHERE user_id='{uid}'"))
    with ThreadPoolExecutor(max_workers=32) as pool:
        codes = list(pool.map(lambda _: api('POST', '/api/progress/lesson', token=t, form={'userId': uid, 'lessonId': free['lesson']})[0], range(100)))
    check('100 concurrent live completion requests', {'200': 100}, {str(k): codes.count(k) for k in set(codes)})
    check('Concurrent completion writes one record', 1, int(db(f"SELECT count(*) FROM lesson_progress WHERE user_id='{uid}' AND lesson_id='{free['lesson']}'")))
    check('Concurrent completion rewards 20 XP once', 20, int(db(f"SELECT xp FROM gamification_stats WHERE user_id='{uid}'")) - xp0)
    _, q = request('Quiz loads without answer keys', 'GET', f"/api/quiz/{c['quiz']}", 200, token=t)
    check('Quiz does not expose correctness', False, 'isCorrect' in json.dumps(q))
    answers = [{'questionId': item['questionId'], 'selectedOptionId': int(db(f"SELECT option_id FROM question_options WHERE question_id={item['questionId']} AND is_correct"))} for item in q['questions']]
    body = {'userId': uid, 'lessonId': c['quiz'], 'answers': answers[:1]}
    _, partial = request('Incomplete quiz rejected', 'POST', '/api/quiz/submit-multiple-choice', 400, body, t)
    check('Partial quiz cannot pass', False, partial.get('passed', False))
    body['answers'] = answers
    _, passed = request('Full quiz submission', 'POST', '/api/quiz/submit-multiple-choice', 200, body, t)
    check('Correct quiz passes', True, passed.get('passed'))
    xp0 = int(db(f"SELECT xp FROM gamification_stats WHERE user_id='{uid}'"))
    _, repeat = request('Repeated full quiz', 'POST', '/api/quiz/submit-multiple-choice', 200, body, t)
    check('Repeated quiz gives zero completion XP', 0, repeat.get('xpRewarded'))
    check('Repeated quiz does not increase total XP', xp0, int(db(f"SELECT xp FROM gamification_stats WHERE user_id='{uid}'")))
    request('Null nested quiz answer rejected', 'POST', '/api/quiz/submit-multiple-choice', 400, dict(body, answers=[{'questionId': None, 'selectedOptionId': None}]), t)
    request('Unenrolled fill-blank answer lookup denied', 'POST', '/api/quiz/fill-blank', 403, {'userId': oid, 'lessonId': c['quiz'], 'answers': [{'questionId': answers[0]['questionId'], 'submittedWord': 'Wrong'}]}, o)
    request('Client cannot forge completed quiz quest', 'POST', f'/api/users/{oid}/daily-quests/progress', 400, {'questType': 'QUIZ_COUNT', 'increment': 10}, o)
    request('AI summary enrolled', 'GET', f"/api/course/{c['id']}/ai-summary", 200, token=t)
    request('Course AI question', 'POST', f"/api/course/{c['id']}/ask", 200, {'question': 'Which database provides transactions?'}, t)
    check('Course vector has 384 dimensions', '384', db(f"SELECT vector_dims(embedding) FROM courses WHERE course_id='{c['id']}'"))
    check('Content lesson vector has 384 dimensions', '384', db(f"SELECT vector_dims(embedding) FROM lesson WHERE lesson_id='{c['lesson']}'"))
    request('Refresh session', 'POST', '/api/auth/refresh', 200, {'refreshToken': login['refreshToken']})
    request('Logout session', 'POST', '/api/auth/logout', 200, {'refreshToken': login['refreshToken']})
    request('Revoked session cannot refresh', 'POST', '/api/auth/refresh', 401, {'refreshToken': login['refreshToken']})
    t = s['outsider']['accessToken']

    def req(name, method, path, body=None):
        return request(name, method, path, 200, body, a)[1]
    req('Admin user directory', 'GET', '/api/admin/users')
    title = 'E2E CRUD ' + s['run']
    req('Admin course create', 'POST', '/api/admin/courses', {'title': title, 'learningModel': 'self-paced', 'status': 'active', 'price': 0, 'description': 'Original text'})
    c = db(f"SELECT course_id FROM courses WHERE title='{title}'")
    s['crudCourse'] = c
    code, b = api('GET', '/api/course', token=t)
    req('Admin course update', 'PUT', f'/api/admin/courses/{c}', {'title': title + ' changed', 'learningModel': 'self-paced', 'status': 'active', 'price': 0, 'description': 'Changed text'})
    check('Course update persisted', title + ' changed', db(f"SELECT title FROM courses WHERE course_id='{c}'"))
    code, b = api('GET', '/api/course', token=t)
    check('Cached catalogue reflects course update', False, title in json.dumps(b) and title + ' changed' not in json.dumps(b))
    b = req('Admin lesson create', 'POST', '/api/admin/lessons', {'courseID': c, 'title': 'CRUD lesson', 'contentType': 'text', 'content': 'Original lesson'})
    l = b['lessonId']
    q = db(f"SELECT lesson_id FROM lesson WHERE parent_lesson_id='{l}'")
    api('GET', f'/api/lesson?courseID={c}', token=t)
    b = req('Admin second lesson create', 'POST', '/api/admin/lessons', {'courseID': c, 'title': 'CRUD second lesson', 'contentType': 'text', 'content': 'New lesson'})
    code, data = api('GET', f'/api/lesson?courseID={c}', token=t)
    check('Cached lesson list reflects new lesson', True, 'CRUD second lesson' in json.dumps(data))
    req('Admin lesson update', 'PUT', f'/api/admin/lessons/{l}', {'title': 'CRUD changed lesson', 'contentType': 'text', 'content': 'Updated lesson'})
    check('Lesson update persisted', 'Updated lesson', db(f"SELECT content FROM lesson WHERE lesson_id='{l}'"))
    req('Admin quiz question create', 'POST', '/api/admin/quizzes', {'lessonId': q, 'questionText': 'CRUD original question', 'points': 10, 'options': [{'optionText': 'Correct', 'isCorrect': True}, {'optionText': 'Wrong', 'isCorrect': False}]})
    qid = db(f"SELECT question_id FROM questions WHERE lesson_id='{q}'")
    req('Admin quiz question update', 'PUT', f'/api/admin/quizzes/{qid}', {'questionText': 'CRUD changed question', 'points': 15})
    check('Question update persisted', 'CRUD changed question', db(f'SELECT question_text FROM questions WHERE question_id={qid}'))
    req('Admin quiz question delete', 'DELETE', f'/api/admin/questions/{qid}')
    check('Question removed', 0, int(db(f'SELECT count(*) FROM questions WHERE question_id={qid}')))
    req('Admin lesson delete', 'DELETE', f'/api/admin/lessons/{l}')
    check('Lesson removed', 0, int(db(f"SELECT count(*) FROM lesson WHERE lesson_id='{l}'")))
    req('Admin course delete', 'DELETE', f'/api/admin/courses/{c}')
    check('Course removed', 0, int(db(f"SELECT count(*) FROM courses WHERE course_id='{c}'")))
    email = 'e2e-delete-' + s['run'] + '@example.test'
    api('POST', '/api/user/register', {'email': email, 'password': s['password'], 'name': 'Deletable', 'active': True})
    uid = db(f"SELECT user_id FROM users WHERE email='{email}'")
    req('Admin user delete', 'DELETE', f'/api/admin/users/{uid}')
    check('User removed', 0, int(db(f"SELECT count(*) FROM users WHERE user_id='{uid}'")))

def main():
    # Store only check outcomes; never write fixture session tokens to the report.
    failure = None
    cleaned = False
    try:
        setup()
        run_checks()
    except Exception as error:
        failure = type(error).__name__
        print('Runner stopped: ' + failure, flush=True)
    finally:
        try:
            cleanup()
            cleaned = True
        except Exception as error:
            failure = 'Cleanup ' + type(error).__name__
            print('FAILED fixture cleanup: ' + type(error).__name__, flush=True)
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(dict(run=s['run'], fixtureCleanup=cleaned, runnerError=failure, checks=results), indent=2) + '\n')
    print(str(sum((item['passed'] for item in results))) + '/' + str(len(results)) + ' checks passed; report: ' + str(args.report))
    return 1 if failure or any((not item['passed'] for item in results)) else 0
if __name__ == '__main__':
    raise SystemExit(main())
