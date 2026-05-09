public class T {
    public static void main(String[] args) {
        String url = "https://www.youtube.com/watch?v=c4R0UnYkv7Y";
        String vId = null;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?:youtu\\.be/|v/|u/\\\\w/|embed/|watch\\?v=)([^#&?]*)");
        java.util.regex.Matcher m = p.matcher(url);
        boolean found = m.find();
        System.out.println("Found: " + found);
        if(found){
            vId = m.group(1);
        }
        System.out.println("ID: " + vId);
    }
}
