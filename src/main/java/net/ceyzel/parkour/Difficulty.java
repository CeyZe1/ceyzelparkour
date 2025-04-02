package net.ceyzel.parkour;

import lombok.Getter;

@Getter
public enum Difficulty {
    EASY(2),
    MEDIUM(5),
    HARD(10),
    EXPERT(25);

    private final int score;

    Difficulty(int score) {
        this.score = score;
    }
}