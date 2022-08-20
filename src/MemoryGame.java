import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MemoryGame {

    static char[] LETTERS = {'A', 'B', 'C', 'D'};
    private final List<String> randomWords;
    private final String level;

    private int health = 10;
    private int columns = 4;
    private int maxWordLength;
    private String[][] displayGrid;
    private Collection<String> uncovered = new ArrayList<>();


    public MemoryGame(int health, int size, String level) throws IOException, URISyntaxException {
        Path path = Paths.get(ClassLoader.getSystemResource("Words.txt").toURI());
        List<String> content = Files.readAllLines(path);

        this.health = health;
        this.columns = size;
        this.level = level;

        this.randomWords = getRandomWords(content, size);
        maxWordLength = randomWords.stream().mapToInt(String::length).max().getAsInt() + 2;

        createGrid();
    }


    public static void main(String[] args) throws IOException, URISyntaxException {

        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Choose the level (e)asy, (h)ard? [e]");
            String level = sc.nextLine();

            int health = 10;
            int size = 4;

            if (level.equalsIgnoreCase("H")) {
                health = 15;
                size = 8;
            }

            MemoryGame memoryGame = new MemoryGame(health, size, level);

            memoryGame.displayGrid(List.of());

            while (health > 0) {
                Collection<Coordinates> userGuesses = new ArrayList<>();

                for (int turn = 0; turn < 2; turn++) {

                    userGuesses.add(memoryGame.askForGuess());

                    memoryGame.displayGrid(userGuesses);
                }
                String maybeWord = memoryGame.guessWasCorrect(userGuesses);
                if (maybeWord != null) {
                    memoryGame.addUncoveredWord(maybeWord);
                } else {
                    health--;
                }
                memoryGame.displayGrid(List.of());
                System.out.println();
                if (memoryGame.areAllWordsUncovered()) {
                    break;
                }
            }

            if (health > 0) {
                System.out.println("Congratulation! You win");
            } else {
                System.out.println("You loose!");
            }

            System.out.println("Do you like play again y/n ? [n]");
            if (sc.nextLine().equalsIgnoreCase("n")) {
                break;
            }
        }
    }

    private Coordinates askForGuess() {
        while (true) {
            System.out.print("Your guess : ");

            String guess = new Scanner(System.in).nextLine().toUpperCase();
            if (guess.equalsIgnoreCase("q")) {
                System.exit(0);
            }
            System.out.println();
            if (guess.matches(String.format("[AB][1-%d]", columns))) {
                int y = Arrays.binarySearch(LETTERS, guess.charAt(0));
                int x = Character.getNumericValue(guess.charAt(1)) - 1;
                return new Coordinates(y, x);
            } else {
                System.out.println("Invalid guess. Please provide valid guess eg. A1");
            }
        }
    }

    private boolean areAllWordsUncovered() {
        return this.uncovered.size() == randomWords.size();
    }

    private void addUncoveredWord(String maybeWord) {
        this.uncovered.add(maybeWord);
    }

    private String guessWasCorrect(Collection<Coordinates> userGuesses) {
        Set<String> words = new HashSet<>();

        for (Coordinates userGuss : userGuesses) {
            words.add(displayGrid[userGuss.y][userGuss.x]);
        }

        return words.size() == 1 ? words.iterator().next() : null;
    }

    private void displayGrid(Collection<Coordinates> userGusses) {
        System.out.println("Level: " + (level.equalsIgnoreCase("H") ? "Hard" : "Easy"));
        System.out.println("Chance: " + health + "\n");
        System.out.print(" ");
        for (int i = 0; i < columns; i++) {
            displayText(String.valueOf(i + 1), maxWordLength);
        }
        System.out.println();
        for (int y = 0; y < 2; y++) {
            System.out.print(LETTERS[y]);

            for (int x = 0; x < columns; x++) {

                String word = displayGrid[y][x];
                if (uncovered.contains(word) || matchUserGuess(y, x, userGusses)) {
                    displayText(word, maxWordLength);
                } else {
                    displayText("x", maxWordLength);
                }
            }
            System.out.println();
        }
    }

    private void displayText(String text, int maxWordLength) {
        int diff = maxWordLength - text.length();
        int padLeft = diff / 2;
        int padRight = diff / 2;

        if (diff % 2 == 1) {
            padLeft++;
        }

        IntStream.range(0, padLeft).forEach(i -> System.out.print(" "));

        System.out.print(text);

        IntStream.range(0, padRight).forEach(i -> System.out.print(" "));

    }

    private boolean matchUserGuess(int y, int x, Collection<Coordinates> userGusses) {
        for (Coordinates userGuss : userGusses) {

            if (x == userGuss.x && y == userGuss.y) {
                return true;
            }
        }

        return false;
    }

    private List<String> getRandomWords(List<String> content, int numberOfWords) {
        Collections.shuffle(content);
        return content.stream().limit(numberOfWords).collect(Collectors.toList());
    }

    private void createGrid() {
        displayGrid = new String[2][columns];
        Collections.shuffle(randomWords);

        displayGrid[0] = randomWords.toArray(new String[0]);
        Collections.shuffle(randomWords);
        displayGrid[1] = randomWords.toArray(new String[0]);
    }

    private static class Coordinates {
        int y;
        int x;

        public Coordinates(int y, int x) {
            this.y = y;
            this.x = x;
        }

    }
}

