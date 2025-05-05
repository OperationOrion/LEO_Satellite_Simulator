package application;

public class SessionData {
    private static int userID;

    public static int getUserID() {
        return userID;
    }

    public static void setUserID(int userID) {
        SessionData.userID = userID;
    }
}