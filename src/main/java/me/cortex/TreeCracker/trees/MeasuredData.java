package me.cortex.TreeCracker.trees;

public class MeasuredData<T> {
    public final MeasureState state;
    public final T measurement;
    public MeasuredData(MeasureState state, T measurement) {
        this.state = state;
        this.measurement = measurement;
    }

    public MeasuredData() {
        this.state = MeasureState.UNKNOWN;
        this.measurement = null;
    }

    public MeasuredData(T measurement) {
        this.state = MeasureState.EXACT;
        this.measurement = measurement;
    }

    public enum MeasureState {
        UNKNOWN,
        EXACT,
        LESS_THAN,
        GREATER_THAN_OR_EQUAL
    }

    public boolean hasMeasurement() {
        return state != MeasureState.UNKNOWN;
    }

    public boolean isExact() {
        return state == MeasureState.EXACT;
    }


}
