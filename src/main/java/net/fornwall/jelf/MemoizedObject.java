package net.fornwall.jelf;

/**
 * A memoized object. Override {@link #computeValue} in subclasses; call {@link #getValue} in using code.
 */
abstract class MemoizedObject<T> {
    private boolean computed;
    private T value;

    @SuppressWarnings("unchecked")
    public static <T> MemoizedObject<T>[] uncheckedArray(int size) {
        return new MemoizedObject[size];
    }

    /**
     * Should compute the value of this memoized object. This will only be called once, upon the first call to
     * {@link #getValue}.
     */
    protected abstract T computeValue() throws ElfException;

    /**
     * Public accessor for the memoized value.
     */
    public final T getValue() throws ElfException {
        if (!computed) {
            value = computeValue();
            computed = true;
        }
        return value;
    }
}