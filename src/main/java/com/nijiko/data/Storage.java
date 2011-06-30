package com.nijiko.data;

import java.util.LinkedHashSet;
import java.util.Set;

import com.nijiko.permissions.EntryType;

public interface Storage {

    public Set<String> getPermissions(String name);
    public void addPermission(String name, String permission);
    public void removePermission(String name, String permission);

    public LinkedHashSet<GroupWorld> getParents(String name);
    public void addParent(String name, String groupWorld, String groupName);
    public void removeParent(String name, String groupWorld, String groupName);
    public void setParents(String name, LinkedHashSet<GroupWorld> groupWs);
    
    public Set<String> getEntries();
    public EntryType getType();
    public boolean create(String name);
    public boolean delete(String name);
    public String getWorld();
    
    public void forceSave();
    public void save();
    public void reload();
    public boolean isAutoSave();
    public void setAutoSave(boolean autoSave);
    
    public String getString(String name, String path);    
    public Integer getInt(String name, String path);    
    public Double getDouble(String name, String path);    
    public Boolean getBool(String name, String path);    
    public void setData(String name, String path, Object data);
    public void removeData(String name, String path);
}
