package site.jnmk.janmaki.npc_tool.tpye;

public enum  SenderType {
    PLAYER,
    CONSOLE;

    public static SenderType getSenderTypeByString(String str){
        for (SenderType senderType:SenderType.values()){
            if (senderType.toString().equalsIgnoreCase(str)){
                return senderType;
            }
        }
        return null;
    }
}
