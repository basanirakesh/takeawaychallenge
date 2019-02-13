import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameOfThree {
    public static void main(String[] args) throws InterruptedException {
        // Number of players playing the game, value should be > 1
        int numberOfPlayers = 2;
        Game game = new Game(numberOfPlayers);

        // Creating player objects and starting the play in a separate thread for each player
        for (int i = 1; i <= numberOfPlayers; i++) {
            Player player = new Player(i);
            System.out.println("Player " + i + " - I am born, get ready to play!!");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        player.play(game);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("Player " + i + " - I am starting my game");
            thread.start();
        }
    }

    /**
     * An object to hold player related information and play logic
     */
    public static class Player {
        int playerNumber = 0;

        Player(int playerNumber) {
            this.playerNumber = playerNumber;
        }

        /**
         * Executes play logic by synchronizing other players
         * @param game
         * @throws InterruptedException
         */
        public void play(Game game) throws InterruptedException {
            while (game.winner == 0) {
                synchronized (game.turns) {
                    // Waiting until previous player's turn is over
                    while (isPreviousPlayerTurnOver(game) == false && game.winner == 0) {
                        System.out.println("Player " + playerNumber + " - waiting for my next turn");
                        game.turns.wait();
                    }

                    // Generating random whole number for the first time (when number is null)
                    if (game.number == null) {
                        Random random = new Random();
                        game.number = random.nextInt(Integer.MAX_VALUE);
                        game.number = 56;
                        System.out.println("Player " + playerNumber + " - random number is generated - " + game.number);
                        changeTurnFlags(game);
                    } else if (game.winner == 0) {
                        runPlayAlgorithm(game);
                    }
                    // Notifying other players who are waiting for their turn
                    game.turns.notifyAll();
                    // Adding sleep for better understanding of execution
                    Thread.sleep(1000);
                }
            }
        }

        /**
         * Runs main play logic
         * @param game
         */
        public void runPlayAlgorithm(Game game) {
            System.out.println("Player " + playerNumber + " - number - " + game.number);
            // Calculation logic - START
            int remainder = game.number % 3;
            if (remainder == 2) {
                game.number++;
                System.out.println("Player " + playerNumber + " - number increased by 1 - " + game.number);
            } else if (remainder == 1) {
                game.number--;
                System.out.println("Player " + playerNumber + " - number decreased by 1 - " + game.number);
            }
            game.number = game.number / 3;
            // Calculation logic - END
            System.out.println("Player " + playerNumber + " - number divided by 3 -   " + game.number);
            // Marking the game winner to current player if result is 1
            if (game.number == 1) {
                System.out.println("Player " + playerNumber + " - Hurrayyyyyy !! I am the winner");
                game.winner = playerNumber;
            }
            changeTurnFlags(game);
        }

        /**
         * Changes turn flags appropriately to progress with the game
         * @param game
         */
        public void changeTurnFlags(Game game) {
            if (playerNumber == 1) {
                // For first player updating the turn status of last player in the map
                game.turns.put(game.turns.size(), false);
            } else {
                // Updating the turn status of previous player
                game.turns.put(playerNumber - 1, false);
            }
            // Changing current player turn status to true, means he/she is done with the calculation and it's next player turn
            game.turns.put(playerNumber, true);
        }

        /**
         * Checks the status of previous player
         * @param game
         * @return
         */
        public boolean isPreviousPlayerTurnOver(Game game) {
            if (playerNumber == 1) {
                // For first player getting the turn status of last player in the map
                return game.turns.get(game.turns.size());
            } else {
                // Getting the turn status of previous player
                return game.turns.get(playerNumber - 1);
            }
        }
    }

    /**
     * An object to hold the number and tracks the turns of each player.
     * This class acts as an interface mentioned in game rules
     */
    public static class Game {
        Integer number = null;
        Map<Integer, Boolean> turns = null;
        int winner = 0;

        Game(int numberOfPlayers) {
            // Initializing the map with number of players defined
            turns = new HashMap<>(numberOfPlayers);
            // Marking initial turn status of each player to false
            for (int i = 1; i <= numberOfPlayers; i++) {
                turns.put(i, false);
            }
            // Marking last player turn to true which makes first player to start the game
            turns.put(numberOfPlayers, true);
        }
    }
}
