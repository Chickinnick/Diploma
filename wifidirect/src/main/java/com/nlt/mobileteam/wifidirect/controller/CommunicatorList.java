package com.nlt.mobileteam.wifidirect.controller;


import com.nlt.mobileteam.wifidirect.utils.exception.CommunicatorListException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The class provides opportunity work with the list of device connections  using familiar
 * the {@code java.util.List} methods.
 * <p>
 * The class contains {@code List<T>} which considers as empty when all its elements
 * contain {@link #emptyCommunicator}.
 * <p>
 * List`s size sets in the constructor {@link #devicesCount}.
 */
public abstract class CommunicatorList<T> implements Iterable<T> {

    private volatile List<T> communicators;
    private final T emptyCommunicator = getEmptyCommunicator();

    private int devicesCount;
    private ReentrantLock lock = new ReentrantLock();

    public CommunicatorList(int devicesCount) {
        this.devicesCount = devicesCount;
        communicators = createEmptyList();
    }

    private List<T> createEmptyList() {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < devicesCount; i++) {
            result.add(emptyCommunicator);
        }
        return result;
    }

    protected abstract T getEmptyCommunicator();


    @Override
    public Iterator<T> iterator() {
        return communicators.iterator();
    }

    /**
     * Return the count of elements which not {@link #emptyCommunicator}
     */
    public int size() {
        int result = devicesCount;

        if (isContainsEmptyCommunicator()) {
            for (T communicator : communicators) {
                if (communicator.equals(emptyCommunicator)) {
                    result--;
                }
            }
        }

        return result;
    }

    public boolean contains(T communicator) {
        return communicators.contains(communicator);
    }

    public int indexOf(T communicator) {
        return communicators.indexOf(communicator);
    }

    public T get(int index) {
        return communicators.get(index);
    }

    /**
     * Replace {@link #emptyCommunicator} with the specified communicator.
     *
     * @return location of added communicator
     * Throws {@link CommunicatorListException} if list not contains {@link #emptyCommunicator}.
     */
    public int add(T communicator) throws CommunicatorListException {
        try {
            lock.lock();
            if (isContainsEmptyCommunicator()) {
                int location = communicators.indexOf(emptyCommunicator);
                communicators.set(location, communicator);
                return location;
            } else {
                throw new CommunicatorListException("Communicator list is full.");
            }
        } finally {
            lock.unlock();
        }

    }

    /**
     * Replace {@link #emptyCommunicator} with the specified communicator to specified location.
     *
     * @return location of added communicator
     * Throws {@link CommunicatorListException} if list not contains {@link #emptyCommunicator}.
     */
    public int add(T communicator, int indexToInsert) throws CommunicatorListException {
        try {
            lock.lock();
            if (isContainsEmptyCommunicator()) {
                if (emptyCommunicator.equals(communicators.get(indexToInsert))) {
                    communicators.set(indexToInsert, communicator);
                } else {
                    throw new CommunicatorListException("Communicator list index " + indexToInsert + " is already taken by another communicator");
                }

                return indexToInsert;
            } else {
                throw new CommunicatorListException("Communicator list is full.");
            }
        } finally {
            lock.unlock();
        }

    }

    public boolean isContainsEmptyCommunicator() {
        return communicators.contains(emptyCommunicator);
    }

    /**
     * Replace the specified communicator with {@link #emptyCommunicator}.
     */
    public T remove(T communicator) {
        int index = communicators.indexOf(communicator);
        if (index >= 0 && index < communicators.size()) {
            return communicators.set(index, emptyCommunicator);
        }
        return emptyCommunicator;
    }

    public T remove(int index) {
        return communicators.set(index, emptyCommunicator);
    }

    public void clear() {
        communicators = createEmptyList();
    }

    public boolean isEmptyCommunicator(int index) {
        return emptyCommunicator.equals(communicators.get(index));
    }

    public boolean isEmptyCommunicator(T communicator) {
        return emptyCommunicator.equals(communicator);
    }

    @Override
    public String toString() {
        return "CommunicatorList{" +
                "communicators=" + communicators +
                ", devicesCount=" + devicesCount +
                '}';
    }
}
