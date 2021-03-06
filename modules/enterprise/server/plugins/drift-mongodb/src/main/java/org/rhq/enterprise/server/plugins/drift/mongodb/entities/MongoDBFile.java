package org.rhq.enterprise.server.plugins.drift.mongodb.entities;

import java.io.Serializable;

import com.google.code.morphia.annotations.Embedded;

import org.rhq.core.domain.drift.DriftFile;
import org.rhq.core.domain.drift.DriftFileStatus;

/**
 * Note that this class might go away. I am not certain that it is needed. Files are
 * stored in fs.files collection when using the GridFS API as we are.
 */
@Embedded
public class MongoDBFile implements Serializable, DriftFile {

    private static final long serialVersionUID = 1L;

    private String hash;

    private Long ctime = System.currentTimeMillis();

    private Long size;

    private DriftFileStatus status;

    public MongoDBFile() {
    }

    public MongoDBFile(String hash) {
        this.hash = hash;
    }
    
    public MongoDBFile(DriftFile driftFile) {
        hash = driftFile.getHashId();
        size = driftFile.getDataSize();
    }

    @Override
    public String getHashId() {
        return hash;
    }

    @Override
    public void setHashId(String hashId) {
        hash = hashId;
    }

    @Override
    public Long getCtime() {
        return ctime;
    }

    @Override
    public Long getDataSize() {
        return size;
    }

    @Override
    public void setDataSize(Long size) {
        this.size = size;
    }

    @Override
    public DriftFileStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(DriftFileStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MongoDBFile that = (MongoDBFile) o;

        if (hash != null ? !hash.equals(that.hash) : that.hash != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hash != null ? hash.hashCode() : 0;
    }
}
