package org.hda.gaf.algorithm.evaluation.direction;

/**
 * Created by marcus on 02.05.16.
 */
public enum RelativeDirection {
    LEFT('l'),
    RIGHT('r'),
    STRAIGHT('s');

    private final char character;

    public char getCharacter() {
        return character;
    }

    RelativeDirection(char c) {
        this.character = c;
    }

    public static RelativeDirection fromChar(char c) {
        if (c == 'l') {
            return LEFT;
        }

        if (c == 'r') {
            return RIGHT;
        }

        if (c == 's') {
            return STRAIGHT;
        }

        throw new IllegalArgumentException("Cannot parse value '" + c + "' to a RelativeDirection. Value must be 'l', 'r' or 's'");
    }
}