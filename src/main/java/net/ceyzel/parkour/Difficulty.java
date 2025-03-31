package net.ceyzel.parkour;

public enum Difficulty {
    EASY(2),
    MEDIUM(5),
    HARD(10),
    EXPERT(25);

    private final int score;

    Difficulty(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}