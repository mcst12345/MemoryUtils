package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.runtime.ObjectMonitor;
import miku.lib.jvm.hotspot.runtime.VMObject;
import miku.lib.jvm.hotspot.utilities.Bits;
import one.helfy.JVM;

public class Mark extends VMObject {
    private static final long ageBits;
    private static final long lockBits;
    private static final long biasedLockBits;
    private static final long maxHashBits;
    private static final long hashBits;
    private static final long lockShift;
    private static final long biasedLockShift;
    private static final long ageShift;
    private static final long hashShift;
    private static final long lockMask;
    private static final long lockMaskInPlace;
    private static final long biasedLockMask;
    private static final long biasedLockMaskInPlace;
    private static final long biasedLockBitInPlace;
    private static final long ageMask;
    private static final long ageMaskInPlace;
    private static final long hashMask;
    private static final long hashMaskInPlace;
    private static final long biasedLockAlignment;
    private static final long lockedValue;
    private static final long unlockedValue;
    private static final long monitorValue;
    private static final long markedValue;
    private static final long biasedLockPattern;
    private static final long noHash;
    private static final long noHashInPlace;
    private static final long noLockInPlace;
    private static final long maxAge;
    private static final long cmsShift;
    private static final long cmsMask;
    private static final long sizeShift;

    static {
        ageBits = JVM.longConstant("markOopDesc::age_bits");
        lockBits = JVM.longConstant("markOopDesc::lock_bits");
        biasedLockBits = JVM.longConstant("markOopDesc::biased_lock_bits");
        maxHashBits = JVM.longConstant("markOopDesc::max_hash_bits");
        hashBits = JVM.longConstant("markOopDesc::hash_bits");
        lockShift = JVM.longConstant("markOopDesc::lock_shift");
        biasedLockShift = JVM.longConstant("markOopDesc::biased_lock_shift");
        ageShift = JVM.longConstant("markOopDesc::age_shift");
        hashShift = JVM.longConstant("markOopDesc::hash_shift");
        lockMask = JVM.longConstant("markOopDesc::lock_mask");
        lockMaskInPlace = JVM.longConstant("markOopDesc::lock_mask_in_place");
        biasedLockMask = JVM.longConstant("markOopDesc::biased_lock_mask");
        biasedLockMaskInPlace = JVM.longConstant("markOopDesc::biased_lock_mask_in_place");
        biasedLockBitInPlace = JVM.longConstant("markOopDesc::biased_lock_bit_in_place");
        ageMask = JVM.longConstant("markOopDesc::age_mask");
        ageMaskInPlace = JVM.longConstant("markOopDesc::age_mask_in_place");
        hashMask = JVM.longConstant("markOopDesc::hash_mask");
        hashMaskInPlace = JVM.longConstant("markOopDesc::hash_mask_in_place");
        biasedLockAlignment = JVM.longConstant("markOopDesc::biased_lock_alignment");
        lockedValue = JVM.longConstant("markOopDesc::locked_value");
        unlockedValue = JVM.longConstant("markOopDesc::unlocked_value");
        monitorValue = JVM.longConstant("markOopDesc::monitor_value");
        markedValue = JVM.longConstant("markOopDesc::marked_value");
        biasedLockPattern = JVM.longConstant("markOopDesc::biased_lock_pattern");
        noHash = JVM.longConstant("markOopDesc::no_hash");
        noHashInPlace = JVM.longConstant("markOopDesc::no_hash_in_place");
        noLockInPlace = JVM.longConstant("markOopDesc::no_lock_in_place");
        maxAge = JVM.longConstant("markOopDesc::max_age");
        cmsShift = JVM.longConstant("markOopDesc::cms_shift");
        cmsMask = JVM.longConstant("markOopDesc::cms_mask");
        sizeShift = JVM.longConstant("markOopDesc::size_shift");
    }

    public Mark(long address) {
        super(address);
    }

    public long value() {
        return unsafe.getInt(getAddress() + JVM.type("oopDesc").offset("_mark"));
    }

    public boolean isCmsFreeChunk() {
        return this.isUnlocked() && (Bits.maskBitsLong(this.value() >> (int) cmsShift, cmsMask) & 1L) == 1L;
    }

    public long getSize() {
        return this.value() >> (int) sizeShift;
    }

    public int age() {
        return (int) Bits.maskBitsLong(this.value() >> (int) ageShift, ageMask);
    }

    public long hash() {
        return Bits.maskBitsLong(this.value() >> (int) hashShift, hashMask);
    }

    public boolean hasNoHash() {
        return this.hash() == noHash;
    }

    public boolean hasMonitor() {
        return (this.value() & monitorValue) != 0L;
    }

    public boolean isLocked() {
        return Bits.maskBitsLong(this.value(), lockMaskInPlace) != unlockedValue;
    }

    public boolean isUnlocked() {
        return Bits.maskBitsLong(this.value(), biasedLockMaskInPlace) == unlockedValue;
    }

    public boolean isMarked() {
        return Bits.maskBitsLong(this.value(), lockMaskInPlace) == markedValue;
    }

    public boolean isBeingInflated() {
        return this.value() == 0L;
    }

    public boolean mustBePreserved() {
        return !this.isUnlocked() || !this.hasNoHash();
    }

    public boolean hasLocker() {
        return (this.value() & lockMaskInPlace) == lockedValue;
    }

    public boolean hasDisplacedMarkHelper() {
        return (this.value() & unlockedValue) == 0L;
    }

    public Mark displacedMarkHelper() {
        if (hasDisplacedMarkHelper()) {
            throw new IllegalStateException();
        }

        return new Mark(getAddress() & (~monitorValue));
    }

    public ObjectMonitor monitor() {
        return new ObjectMonitor(getAddress() ^ monitorValue);
    }
}
