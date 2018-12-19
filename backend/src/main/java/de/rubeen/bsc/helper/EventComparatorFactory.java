package de.rubeen.bsc.helper;

import de.rubeen.bsc.entities.web.EventEntity;

import java.util.Comparator;

public class EventComparatorFactory {

    private EventComparatorFactory() {
    }

    public static ComparableByDate getDateComparator() {
        return new ComparableByDate();
    }

    public static ComparableByName getNameComparator() {
        return new ComparableByName();
    }

    private static class ComparableByName implements Comparator<EventEntity> {

        @Override
        public int compare(EventEntity o1, EventEntity o2) {
            return o1.getSubject().compareTo(o2.getSubject());
        }
    }

    private static class ComparableByDate implements Comparator<EventEntity> {

        @Override
        public int compare(EventEntity o1, EventEntity o2) {
            return o1.getStartTime().compareTo(o2.getStartTime());
        }
    }
}
