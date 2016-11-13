package gs.ibeacon.fcu.slideswipe.JSON;

/**
 * Created by bing on 2016/6/10.
 */
public class JSON {


    public static final String KEY_USER_NAME = "USER_NAME" ;
    public static final String KEY_USER_PWD = "USER_PWD" ;
    public static final String KEY_RSSI = "RSSI" ;
    public static final String KEY_UUID = "UUID" ;
    public static final String KEY_MAJOR = "MAJOR" ;
    public static final String KEY_MINOR = "MINOR" ;
    public static final String KEY_LOCATION = "LOCATION" ;
    public static final String KEY_FRIEND_LIST = "好友清單";
    public static final String KEY_OTHERUSER_LIST = "其他用戶清單";
    public static final String KEY_BINDING = "BINDING";
    public static final String KEY_OTHER_USER = "對方";
    public static final String KEY_LOST_TIME = "遺失時間";
    public static final String KEY_LOST_COST = "懸賞金額";
    public static final String KEY_LOST_DESCRIPTION = "遺失內容";



    public static final String KEY_RESULT = "result";
    public static final String KEY_RESULT_MESSAGE = "resultMessage";
    public static final String KEY_TARGET_LOCATION = "targetLocation";
    public static final String KEY_ITEM_NAME = "itemName";
    public static final String KEY_ITEM_LIST = "itemList";
    public static final String KEY_ITEM_LOCATION = "itemLocation";
    public static final String KEY_OTHER_USER_PERMISION = "是否允許請求所在位置";
    public static final String KEY_IS_MY_ITEM_OR_NOT = "";
    public static final String KEY_MOVE_TO_TARGET_LOCATION = "movetargetLocation";
    public static final String KEY_MOVE_TO_TARGET_PATH = "movetargetLocation";


    public static final String KEY_STATE = "STATE" ;
    public static final int STATE_LOGOUT = 0 ;
    public static final int STATE_SEND_IBEACON = 1 ;
    public static final int STATE_LOGIN = 2 ;
    public static final int STATE_WHOAMI = 3 ;
    public static final int STATE_FIND_FRIEND = 4 ;
    public static final int STATE_CAR_BINDING = 5 ;
    public static final int STATE_USER_MOVE = 6 ;
    public static final int STATE_FIND_TARGET_LOCATION = 7 ;
    public static final int STATE_FIND_ITEM_LIST = 8 ;
    public static final int STATE_SEND_ITEM_IBEACON = 9 ;
    public static final int STATE_ASK_LOCATION_PERMISSION = 10 ;
    public static final int STATE_RETURN_ASK_LOCATION_PERMISSION = 11 ;
    public static final int STATE_GET_ITEM_LOCATION = 12 ;
    public static final int STATE_POST_ITEM = 13 ;
    public static final int STATE_IS_MY_ITEM_OR_NOT = 14;
    public static final int STATE_RETURN_IS_OR_NOT_MY_ITEM = 15;
    public static final int STATE_SEND_LOST_ITEM_LOCATION = 16;
    public static final int STATE_FOUND_LOST_ITEM = 17;
    public static final int STATE_ITEM_RSSI_TOO_FAR = 18;
    public static final int STATE_MOVE_TO_TARGET = 19;
    public static final int STATE_MOVE_TO_TARGET_PATH = 20;
    public static final int STATE_AUTO_FOLLOW = 21;


    public static final String MESSAGE_NOLOATION = "noLocation";


}