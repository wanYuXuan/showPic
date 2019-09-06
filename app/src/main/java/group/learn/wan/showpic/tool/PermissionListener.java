package group.learn.wan.showpic.tool;

import java.util.List;

public interface PermissionListener {
    void granted();
    void denied(List<String>deniedList);
}
