public class TestRegex {
    public static void main(String[] args) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("^https?://.*(?:youtu.be/|v/|u/\\\\w/|embed/|watch\\\\?v=)([^#&?]*).*$", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m = p.matcher("https://www.youtube.com/watch?v=c4R0UnYkv7Y");
        if(m.matches()) System.out.println("ID: [" + m.group(1) + "]");
        else System.out.println("NO MATCH");
    }
}
