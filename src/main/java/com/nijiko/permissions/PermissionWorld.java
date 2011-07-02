package com.nijiko.permissions;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.nijiko.data.GroupStorage;
import com.nijiko.data.GroupWorld;
import com.nijiko.data.UserStorage;
import com.nijikokun.bukkit.Permissions.Permissions;

public class PermissionWorld {

    private final ModularControl controller;
    private final String world;
    private UserStorage userStore;
    private GroupStorage groupStore;
    private Map<String, User> users = new HashMap<String, User>();
    private Map<String, Group> groups = new HashMap<String, Group>();
    private Group defaultGroup;

    PermissionWorld(String world, ModularControl controller, UserStorage userStore, GroupStorage groupStore) {
        this.world = world;
        this.userStore = userStore;
        this.groupStore = groupStore;
        this.controller = controller;
    }

    public boolean reload() {
        if (world.equals("?") || userStore == null || groupStore == null || world.equals("?"))// Yes, only a minimum of one is needed.
            return false;

        if (userStore != null)
            userStore.reload();
        if (groupStore != null)
            groupStore.reload();

        defaultGroup = null;

        Map<String, Group> oldGroups = groups;
        Map<String, Group> newGroups = new HashMap<String, Group>();

        Set<String> groupNames = groupStore.getEntries();
        for (String groupName : groupNames) {
            Group group = new Group(groupStore, groupName, this, false);
            Group oldGroup = oldGroups.get(groupName.toLowerCase());
            if (oldGroup != null) {
                group.copyTimedMap(oldGroup);
            }
            newGroups.put(groupName.toLowerCase(), group);
            if (group.isDefault() && defaultGroup == null)
                defaultGroup = group;
        }
        groups = newGroups;
        oldGroups = null;

        Map<String, User> oldUsers = users;
        Map<String, User> newUsers = new HashMap<String, User>();

        Set<String> userNames = userStore.getEntries();
        for (String userName : userNames) {
            User user = new User(userStore, userName, this, false);
            User oldUser = oldUsers.get(userName.toLowerCase());
            if (oldUser != null) {
                user.copyTimedMap(oldUser);
            }
            newUsers.put(userName.toLowerCase(), user);
        }
        users = newUsers;
        oldUsers = null;

        Permissions.instance.getServer().getPluginManager().callEvent(new WorldConfigLoadEvent(world));
        return true;
    }

    public void minorReload() {
        if (userStore != null)
            userStore.reload();
        if (groupStore != null)
            groupStore.reload();

        for (User u : users.values()) {
            u.clearTransientPerms();
            u.clearCache();
        }
        for (Group g : groups.values()) {
            g.clearTransientPerms();
            g.clearCache();
        }
    }

    public void clearCaches() {
        for (User u : users.values()) {
            u.clearCache();
        }
        for (Group g : groups.values()) {
            g.clearCache();
        }
    }

    public void save() {
        if (userStore != null)
            userStore.forceSave();
        if (groupStore != null)
            groupStore.forceSave();
    }

    public User getUserObject(String name) {
        return users.get(name.toLowerCase());
    }

    public Group getGroupObject(String name) {
        return groups.get(name.toLowerCase());
    }

    public User safeGetUser(String name) {
        User u = users.get(name.toLowerCase());
        if (u == null) {
            u = new User(userStore, name, this, true);
            users.put(name.toLowerCase(), u);
        }
        return u;
    }

    public Group safeGetGroup(String name) {
        Group g = groups.get(name.toLowerCase());
        if (g == null) {
            g = new Group(groupStore, name, this, true);
            groups.put(name.toLowerCase(), g);
        }
        return g;
    }

    public Group getDefaultGroup() {
        return defaultGroup;
    }

    public UserStorage getUserStorage() {
        return userStore;
    }

    public GroupStorage getGroupStorage() {
        return groupStore;
    }

    public boolean deleteUser(String name) {
        User u = this.getUserObject(name);
        if (u != null) {
            return u.delete();
        }
        return false;
    }

    void delUsr(String name) {
        users.remove(name.toLowerCase());
    }

    public boolean deleteGroup(String name) {
        Group g = this.getGroupObject(name);
        if (g != null) {
            return g.delete();
        }
        return false;
    }

    void delGrp(String name) {
        Group g = groups.remove(name.toLowerCase());
        if (g != null && g.equals(defaultGroup))
            defaultGroup = null;
    }

    public Collection<User> getUsers() {
        return users.values();
    }

    public Collection<Group> getGroups() {
        return groups.values();
    }

    public Set<String> getTracks() {
        return groupStore.getTracks();
    }

    public String getWorldName() {
        return world;
    }

    void tick(long interval) {
        for (User u : users.values())
            u.tick(interval);
    }

    @Override
    public String toString() {
        return "PermissionWorld " + world;
    }

    // Methods used by Entry to wrap calls to ModularControl
    void clearAllCaches() {
        controller.clearAllCaches();
    }

    LinkedHashSet<Group> stringToGroups(LinkedHashSet<GroupWorld> raws, String overrideWorld) {
        LinkedHashSet<Group> groupSet = new LinkedHashSet<Group>();
        if (raws == null)
            return groupSet;
        for (GroupWorld raw : raws) {
            String rawWorld = raw.getWorld();
            if (rawWorld.equals("?") && overrideWorld != null) {
                rawWorld = overrideWorld;
            }
            Group g = this.getGrp(rawWorld, raw.getName());
            if (g != null)
                groupSet.add(g);
        }
        return groupSet;
    }

    PermissionWorld getWorldParent(boolean isUser) {
        String parentName = controller.getWorldParent(world, isUser);
//        System.out.println(parentName);
        if(parentName == null)
            return null;
        try {
            return controller.safeGetWorld(parentName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    User getUsr(String world, String name) {
        if (this.world.equals(world)) {
            return this.getUsr(name);
        }
        return controller.getUsr(world, name);
    }

    Group getGrp(String world, String name) {
        if (this.world.equals(world)) {
            return this.getGrp(name);
        }
        return controller.getGrp(world, name);
    }

    User getUsr(String name) {
        return this.safeGetUser(name);
    }

    Group getGrp(String name) {
        return this.safeGetGroup(name);
    }

    User getUserObject(String world, String name) {
        if (this.world.equals(world)) {
            return this.getUserObject(name);
        }
        return controller.getUserObject(world, name);
    }
}
