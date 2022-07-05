package io.bootique.tools.release.view;

public class ReleaseStatusMsg {

    private String name;
    private String color;
    private String msg;

    public ReleaseStatusMsg(){}

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        switch (msg) {
            case "Done", "Process" -> this.color = "green";
            case "Wait"            -> this.color = "red";
        }
        this.msg = msg;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }
}
