package friendlyitsolution.com.filemanager2done;

public class ContactModel {

    String name,id,path,time,type;

    public ContactModel(String Cid, String Cname, String path, String time, String type)
    {
        this.time=time;
        this.type=type;
        this.id=Cid;
        this.name=Cname;
        this.path=path;

    }


}
