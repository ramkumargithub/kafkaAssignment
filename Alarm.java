import java.sql.Timestamp;

public class Alarm {

    private int id;
    private String affectedNode;
    private String vnocAlarmID;
    private Timestamp alarmEventTime;

    public Alarm(int id, String affectedNode, String vnocAlarmID, Timestamp alarmEventTime) {
        this.id = id;
        this.affectedNode = affectedNode;
        this.vnocAlarmID = vnocAlarmID;
        this.alarmEventTime = alarmEventTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAffectedNode() {
        return affectedNode;
    }

    public void setAffectedNode(String affectedNode) {
        this.affectedNode = affectedNode;
    }

    public String getVnocAlarmID() {
        return vnocAlarmID;
    }

    public void setVnocAlarmID(String vnocAlarmID) {
        this.vnocAlarmID = vnocAlarmID;
    }

    public Timestamp getAlarmEventTime() {
        return alarmEventTime;
    }

    public void setAlarmEventTime(Timestamp alarmEventTime) {
        this.alarmEventTime = alarmEventTime;
    }
}
