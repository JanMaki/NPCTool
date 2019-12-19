package site.jnmk.janmaki.npc_tool.tpye;

public enum ClickType {
    LEFT,
    RIGHT,
    ALL;

    public static ClickType getClickTypeByString(String str){
        for (ClickType clickType:ClickType.values()){
            if (clickType.toString().equalsIgnoreCase(str)){
                return clickType;
            }
        }
        return null;
    }
}
