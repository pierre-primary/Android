/**
 * @author oopp1990
 */
package com.android.bitmap.core;


import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.Adler32;

public class DiskCache implements Closeable {
    private static final String TAG = DiskCache.class.getSimpleName();

    private static final int MAGIC_INDEX_FILE = 0xB3273030;
    private static final int MAGIC_DATA_FILE = 0xBD248510;

    // index header offset
    private static final int IH_MAGIC = 0;
    private static final int IH_MAX_ENTRIES = 4;
    private static final int IH_MAX_BYTES = 8;
    private static final int IH_ACTIVE_REGION = 12;
    private static final int IH_ACTIVE_ENTRIES = 16;
    private static final int IH_ACTIVE_BYTES = 20;
    private static final int IH_VERSION = 24;
    private static final int IH_CHECKSUM = 28;
    private static final int INDEX_HEADER_SIZE = 32;

    private static final int DATA_HEADER_SIZE = 4;

    // blob header offset
    private static final int BH_KEY = 0;
    private static final int BH_CHECKSUM = 8;
    private static final int BH_OFFSET = 12;
    private static final int BH_LENGTH = 16;
    private static final int BLOB_HEADER_SIZE = 20;

    private RandomAccessFile mIndexFile;
    private RandomAccessFile mDataFile0;
    private RandomAccessFile mDataFile1;
    private FileChannel mIndexChannel;
    private MappedByteBuffer mIndexBuffer;

    private int mMaxEntries;
    private int mMaxBytes;
    private int mActiveRegion;
    private int mActiveEntries;
    private int mActiveBytes;
    private int mVersion;

    private RandomAccessFile mActiveDataFile;
    private RandomAccessFile mInactiveDataFile;
    private int mActiveHashStart;
    private int mInactiveHashStart;
    private byte[] mIndexHeader = new byte[INDEX_HEADER_SIZE];
    private byte[] mBlobHeader = new byte[BLOB_HEADER_SIZE];
    private Adler32 mAdler32 = new Adler32();
    private String mPath;

    /**
     * 这里会创建 ".idx"和".0"和".1"文件，".0"和".1"是数据缓存，他们只有一个“激活”状态的，当缓存数据达到用户配置量的时候会清空一个数据，然后切换另一个为“激活”状态
     *
     * @param path       缓存路径
     * @param maxEntries 存放的最大item 容量
     * @param maxBytes   存放的最大数据容量
     * @param reset      是否重置，如果true，则先清空所有数据才能使用
     * @throws java.io.IOException
     */
    public DiskCache(String path, int maxEntries, int maxBytes, boolean reset) throws IOException {
        this(path, maxEntries, maxBytes, reset, 0);
    }

    public DiskCache(String path, int maxEntries, int maxBytes, boolean reset, int version) throws IOException {
        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("unable to make dirs");
            }
        }
        mPath = path;
        mIndexFile = new RandomAccessFile(path + ".idx", "rw");
        mDataFile0 = new RandomAccessFile(path + ".0", "rw");
        mDataFile1 = new RandomAccessFile(path + ".1", "rw");
        mVersion = version;

        if (!reset && loadIndex()) {
            return;
        }

        resetCache(maxEntries, maxBytes);

        if (!loadIndex()) {
            closeAll();
            throw new IOException("unable to load index");
        }
    }

    // Delete the files associated with the given path previously created
    // by the BlobCache constructor.
    public void delete() {
        deleteFileSilently(mPath + ".idx");
        deleteFileSilently(mPath + ".0");
        deleteFileSilently(mPath + ".1");
    }

    private static void deleteFileSilently(String path) {
        try {
            new File(path).delete();
        } catch (Throwable t) {
            // ignore;
        }
    }

    // Close the cache. All resources are released. No other method should be
    // called after this is called.
    @Override
    public void close() {
        syncAll();
        closeAll();
    }

    private void closeAll() {
        closeSilently(mIndexChannel);
        closeSilently(mIndexFile);
        closeSilently(mDataFile0);
        closeSilently(mDataFile1);
    }

    // Returns true if loading index is successful. After this method is called,
    // mIndexHeader and index header in file should be kept sync.
    private boolean loadIndex() {
        try {
            mIndexFile.seek(0);
            mDataFile0.seek(0);
            mDataFile1.seek(0);

            byte[] buf = mIndexHeader;
            if (mIndexFile.read(buf) != INDEX_HEADER_SIZE) {
                Log.w(TAG, "cannot read header");
                return false;
            }

            if (readInt(buf, IH_MAGIC) != MAGIC_INDEX_FILE) {
                Log.w(TAG, "cannot read header magic");
                return false;
            }

            if (readInt(buf, IH_VERSION) != mVersion) {
                Log.w(TAG, "version mismatch");
                return false;
            }

            mMaxEntries = readInt(buf, IH_MAX_ENTRIES);
            mMaxBytes = readInt(buf, IH_MAX_BYTES);
            mActiveRegion = readInt(buf, IH_ACTIVE_REGION);
            mActiveEntries = readInt(buf, IH_ACTIVE_ENTRIES);
            mActiveBytes = readInt(buf, IH_ACTIVE_BYTES);

            int sum = readInt(buf, IH_CHECKSUM);
            if (checkSum(buf, 0, IH_CHECKSUM) != sum) {
                Log.w(TAG, "header checksum does not match");
                return false;
            }

            // Sanity check
            if (mMaxEntries <= 0) {
                Log.w(TAG, "invalid max entries");
                return false;
            }
            if (mMaxBytes <= 0) {
                Log.w(TAG, "invalid max bytes");
                return false;
            }
            if (mActiveRegion != 0 && mActiveRegion != 1) {
                Log.w(TAG, "invalid active region");
                return false;
            }
            if (mActiveEntries < 0 || mActiveEntries > mMaxEntries) {
                Log.w(TAG, "invalid active entries");
                return false;
            }
            if (mActiveBytes < DATA_HEADER_SIZE || mActiveBytes > mMaxBytes) {
                Log.w(TAG, "invalid active bytes");
                return false;
            }
            if (mIndexFile.length() !=
                    INDEX_HEADER_SIZE + mMaxEntries * 12 * 2) {
                Log.w(TAG, "invalid index file length");
                return false;
            }

            // Make sure data file has magic
            byte[] magic = new byte[4];
            if (mDataFile0.read(magic) != 4) {
                Log.w(TAG, "cannot read data file magic");
                return false;
            }
            if (readInt(magic, 0) != MAGIC_DATA_FILE) {
                Log.w(TAG, "invalid data file magic");
                return false;
            }
            if (mDataFile1.read(magic) != 4) {
                Log.w(TAG, "cannot read data file magic");
                return false;
            }
            if (readInt(magic, 0) != MAGIC_DATA_FILE) {
                Log.w(TAG, "invalid data file magic");
                return false;
            }

            // Map index file to memory
            mIndexChannel = mIndexFile.getChannel();
            mIndexBuffer = mIndexChannel.map(FileChannel.MapMode.READ_WRITE,
                    0, mIndexFile.length());
            mIndexBuffer.order(ByteOrder.LITTLE_ENDIAN);

            setActiveVariables();
            return true;
        } catch (IOException ex) {
            Log.e(TAG, "loadIndex failed.", ex);
            return false;
        }
    }

    private void setActiveVariables() throws IOException {
        mActiveDataFile = (mActiveRegion == 0) ? mDataFile0 : mDataFile1;
        mInactiveDataFile = (mActiveRegion == 1) ? mDataFile0 : mDataFile1;
        mActiveDataFile.setLength(mActiveBytes);
        mActiveDataFile.seek(mActiveBytes);

        mActiveHashStart = INDEX_HEADER_SIZE;
        mInactiveHashStart = INDEX_HEADER_SIZE;

        if (mActiveRegion == 0) {
            mInactiveHashStart += mMaxEntries * 12;
        } else {
            mActiveHashStart += mMaxEntries * 12;
        }
    }

    private void resetCache(int maxEntries, int maxBytes) throws IOException {
        mIndexFile.setLength(0);  // truncate to zero the index
        mIndexFile.setLength(INDEX_HEADER_SIZE + maxEntries * 12 * 2);
        mIndexFile.seek(0);
        byte[] buf = mIndexHeader;
        writeInt(buf, IH_MAGIC, MAGIC_INDEX_FILE);
        writeInt(buf, IH_MAX_ENTRIES, maxEntries);
        writeInt(buf, IH_MAX_BYTES, maxBytes);
        writeInt(buf, IH_ACTIVE_REGION, 0);
        writeInt(buf, IH_ACTIVE_ENTRIES, 0);
        writeInt(buf, IH_ACTIVE_BYTES, DATA_HEADER_SIZE);
        writeInt(buf, IH_VERSION, mVersion);
        writeInt(buf, IH_CHECKSUM, checkSum(buf, 0, IH_CHECKSUM));
        mIndexFile.write(buf);
        // This is only needed if setLength does not zero the extended part.
        // writeZero(mIndexFile, maxEntries * 12 * 2);

        mDataFile0.setLength(0);
        mDataFile1.setLength(0);
        mDataFile0.seek(0);
        mDataFile1.seek(0);
        writeInt(buf, 0, MAGIC_DATA_FILE);
        mDataFile0.write(buf, 0, 4);
        mDataFile1.write(buf, 0, 4);
    }

    // Flip the active region and the inactive region.
    private void flipRegion() throws IOException {
        mActiveRegion = 1 - mActiveRegion;
        mActiveEntries = 0;
        mActiveBytes = DATA_HEADER_SIZE;

        writeInt(mIndexHeader, IH_ACTIVE_REGION, mActiveRegion);
        writeInt(mIndexHeader, IH_ACTIVE_ENTRIES, mActiveEntries);
        writeInt(mIndexHeader, IH_ACTIVE_BYTES, mActiveBytes);
        updateIndexHeader();

        setActiveVariables();
        clearHash(mActiveHashStart);
        syncIndex();
    }

    // Sync mIndexHeader to the index file.
    private void updateIndexHeader() {
        writeInt(mIndexHeader, IH_CHECKSUM,
                checkSum(mIndexHeader, 0, IH_CHECKSUM));
        mIndexBuffer.position(0);
        mIndexBuffer.put(mIndexHeader);
    }

    // Clear the hash table starting from the specified offset.
    private void clearHash(int hashStart) {
        byte[] zero = new byte[1024];
        mIndexBuffer.position(hashStart);
        for (int count = mMaxEntries * 12; count > 0; ) {
            int todo = Math.min(count, 1024);
            mIndexBuffer.put(zero, 0, todo);
            count -= todo;
        }
    }

    // Inserts a (key, data) pair into the cache.
    public void insert(long key, byte[] data) throws IOException {
        if (DATA_HEADER_SIZE + BLOB_HEADER_SIZE + data.length > mMaxBytes) {
            throw new RuntimeException("blob is too large!");
        }

        if (mActiveBytes + BLOB_HEADER_SIZE + data.length > mMaxBytes
                || mActiveEntries * 2 >= mMaxEntries) {
            flipRegion();
        }

        if (!lookupInternal(key, mActiveHashStart)) {
            // If we don't have an existing entry with the same key, increase
            // the entry count.
            mActiveEntries++;
            writeInt(mIndexHeader, IH_ACTIVE_ENTRIES, mActiveEntries);
        }

        insertInternal(key, data, data.length);
        updateIndexHeader();
    }

    // Appends the data to the active file. It also updates the hash entry.
    // The proper hash entry (suitable for insertion or replacement) must be
    // pointed by mSlotOffset.
    private void insertInternal(long key, byte[] data, int length)
            throws IOException {
        byte[] header = mBlobHeader;
        int sum = checkSum(data);
        writeLong(header, BH_KEY, key);
        writeInt(header, BH_CHECKSUM, sum);
        writeInt(header, BH_OFFSET, mActiveBytes);
        writeInt(header, BH_LENGTH, length);
        mActiveDataFile.write(header);
        mActiveDataFile.write(data, 0, length);

        mIndexBuffer.putLong(mSlotOffset, key);
        mIndexBuffer.putInt(mSlotOffset + 8, mActiveBytes);
        mActiveBytes += BLOB_HEADER_SIZE + length;
        writeInt(mIndexHeader, IH_ACTIVE_BYTES, mActiveBytes);
    }

    public static class LookupRequest {
        public long key;        // input: the key to find
        public byte[] buffer;   // input/output: the buffer to store the blob
        public int length;      // output: the length of the blob
    }

    // This method is for one-off lookup. For repeated lookup, use the version
    // accepting LookupRequest to avoid repeated memory allocation.
    private LookupRequest mLookupRequest = new LookupRequest();

    public byte[] lookup(long key) throws IOException {
        mLookupRequest.key = key;
        mLookupRequest.buffer = null;
        if (lookup(mLookupRequest)) {
            return mLookupRequest.buffer;
        } else {
            return null;
        }
    }

    // Returns true if the associated blob for the given key is available.
    // The blob is stored in the buffer pointed by req.buffer, and the length
    // is in stored in the req.length variable.
    //
    // The user can input a non-null value in req.buffer, and this method will
    // try to use that buffer. If that buffer is not large enough, this method
    // will allocate a new buffer and assign it to req.buffer.
    //
    // This method tries not to throw IOException even if the data file is
    // corrupted, but it can still throw IOException if things get strange.
    public boolean lookup(LookupRequest req) throws IOException {
        // Look up in the active region first.
        if (lookupInternal(req.key, mActiveHashStart)) {
            if (getBlob(mActiveDataFile, mFileOffset, req)) {
                return true;
            }
        }

        // We want to copy the data from the inactive file to the active file
        // if it's available. So we keep the offset of the hash entry so we can
        // avoid looking it up again.
        int insertOffset = mSlotOffset;

        // Look up in the inactive region.
        if (lookupInternal(req.key, mInactiveHashStart)) {
            if (getBlob(mInactiveDataFile, mFileOffset, req)) {
                // If we don't have enough space to insert this blob into
                // the active file, just return it.
                if (mActiveBytes + BLOB_HEADER_SIZE + req.length > mMaxBytes
                        || mActiveEntries * 2 >= mMaxEntries) {
                    return true;
                }
                // Otherwise copy it over.
                mSlotOffset = insertOffset;
                try {
                    insertInternal(req.key, req.buffer, req.length);
                    mActiveEntries++;
                    writeInt(mIndexHeader, IH_ACTIVE_ENTRIES, mActiveEntries);
                    updateIndexHeader();
                } catch (Throwable t) {
                    Log.e(TAG, "cannot copy over");
                }
                return true;
            }
        }

        return false;
    }


    // Copies the blob for the specified offset in the specified file to
    // req.buffer. If req.buffer is null or too small, allocate a buffer and
    // assign it to req.buffer.
    // Returns false if the blob is not available (either the index file is
    // not sync with the data file, or one of them is corrupted). The length
    // of the blob is stored in the req.length variable.
    private boolean getBlob(RandomAccessFile file, int offset,
                            LookupRequest req) throws IOException {
        byte[] header = mBlobHeader;
        long oldPosition = file.getFilePointer();
        try {
            file.seek(offset);
            if (file.read(header) != BLOB_HEADER_SIZE) {
                Log.w(TAG, "cannot read blob header");
                return false;
            }
            long blobKey = readLong(header, BH_KEY);
            if (blobKey != req.key) {
                Log.w(TAG, "blob key does not match: " + blobKey);
                return false;
            }
            int sum = readInt(header, BH_CHECKSUM);
            int blobOffset = readInt(header, BH_OFFSET);
            if (blobOffset != offset) {
                Log.w(TAG, "blob offset does not match: " + blobOffset);
                return false;
            }
            int length = readInt(header, BH_LENGTH);
            if (length < 0 || length > mMaxBytes - offset - BLOB_HEADER_SIZE) {
                Log.w(TAG, "invalid blob length: " + length);
                return false;
            }
            if (req.buffer == null || req.buffer.length < length) {
                req.buffer = new byte[length];
            }

            byte[] blob = req.buffer;
            req.length = length;

            if (file.read(blob, 0, length) != length) {
                Log.w(TAG, "cannot read blob data");
                return false;
            }
            if (checkSum(blob, 0, length) != sum) {
                Log.w(TAG, "blob checksum does not match: " + sum);
                return false;
            }
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "getBlob failed.", t);
            return false;
        } finally {
            file.seek(oldPosition);
        }
    }

    // Tries to look up a key in the specified hash region.
    // Returns true if the lookup is successful.
    // The slot offset in the index file is saved in mSlotOffset. If the lookup
    // is successful, it's the slot found. Otherwise it's the slot suitable for
    // insertion.
    // If the lookup is successful, the file offset is also saved in
    // mFileOffset.
    private int mSlotOffset;
    private int mFileOffset;

    private boolean lookupInternal(long key, int hashStart) {
        int slot = (int) (key % mMaxEntries);
        if (slot < 0) slot += mMaxEntries;
        int slotBegin = slot;
        while (true) {
            int offset = hashStart + slot * 12;
            long candidateKey = mIndexBuffer.getLong(offset);
            int candidateOffset = mIndexBuffer.getInt(offset + 8);
            if (candidateOffset == 0) {
                mSlotOffset = offset;
                return false;
            } else if (candidateKey == key) {
                mSlotOffset = offset;
                mFileOffset = candidateOffset;
                return true;
            } else {
                if (++slot >= mMaxEntries) {
                    slot = 0;
                }
                if (slot == slotBegin) {
                    Log.w(TAG, "corrupted index: clear the slot.");
                    mIndexBuffer.putInt(hashStart + slot * 12 + 8, 0);
                }
            }
        }
    }

    public void syncIndex() {
        try {
            mIndexBuffer.force();
        } catch (Throwable t) {
            Log.w(TAG, "sync index failed", t);
        }
    }

    public void syncAll() {
        syncIndex();
        try {
            mDataFile0.getFD().sync();
        } catch (Throwable t) {
            Log.w(TAG, "sync data file 0 failed", t);
        }
        try {
            mDataFile1.getFD().sync();
        } catch (Throwable t) {
            Log.w(TAG, "sync data file 1 failed", t);
        }
    }

    // This is for testing only.
    //
    // Returns the active count (mActiveEntries). This also verifies that
    // the active count matches matches what's inside the hash region.
    int getActiveCount() {
        int count = 0;
        for (int i = 0; i < mMaxEntries; i++) {
            int offset = mActiveHashStart + i * 12;
//            long candidateKey = mIndexBuffer.getLong(offset);
            int candidateOffset = mIndexBuffer.getInt(offset + 8);
            if (candidateOffset != 0) ++count;
        }
        if (count == mActiveEntries) {
            return count;
        } else {
            Log.e(TAG, "wrong active count: " + mActiveEntries + " vs " + count);
            return -1;  // signal failure.
        }
    }

    int checkSum(byte[] data) {
        mAdler32.reset();
        mAdler32.update(data);
        return (int) mAdler32.getValue();
    }

    int checkSum(byte[] data, int offset, int nbytes) {
        mAdler32.reset();
        mAdler32.update(data, offset, nbytes);
        return (int) mAdler32.getValue();
    }

    static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }

    static int readInt(byte[] buf, int offset) {
        return (buf[offset] & 0xff)
                | ((buf[offset + 1] & 0xff) << 8)
                | ((buf[offset + 2] & 0xff) << 16)
                | ((buf[offset + 3] & 0xff) << 24);
    }

    static long readLong(byte[] buf, int offset) {
        long result = buf[offset + 7] & 0xff;
        for (int i = 6; i >= 0; i--) {
            result = (result << 8) | (buf[offset + i] & 0xff);
        }
        return result;
    }

    static void writeInt(byte[] buf, int offset, int value) {
        for (int i = 0; i < 4; i++) {
            buf[offset + i] = (byte) (value & 0xff);
            value >>= 8;
        }
    }

    static void writeLong(byte[] buf, int offset, long value) {
        for (int i = 0; i < 8; i++) {
            buf[offset + i] = (byte) (value & 0xff);
            value >>= 8;
        }
    }
}
