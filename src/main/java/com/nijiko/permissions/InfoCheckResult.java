package com.nijiko.permissions;

public class InfoCheckResult {
    private final Entry checked;
    private final String path;
    private final Entry source;
    private final Object data;
    private final String world;
    private final boolean skipCache;
    private boolean valid = true;
    private InfoCheckResult parent;

    public InfoCheckResult(Entry checked, String path, Entry source, Object data, String world, boolean skipCache) {
        this(checked, path, source, data, world, skipCache, null);
    }
    public InfoCheckResult(Entry checked, String path, Entry source, Object data, String world, boolean skipCache, InfoCheckResult parent) {
        this.checked = checked;
        this.path = path;
        this.source = source;
        this.data = data;
        this.world = world;
        this.skipCache = skipCache;
        this.parent = parent;
    }
    
}
