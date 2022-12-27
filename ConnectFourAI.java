package ICS4U1;

import java.util.*;
import java.io.*;

/*
Class: ConnectFourAI.java
Name: Harry Liu
Date; June 20, 2022
Description: This program is a text-based version of the game Connect Four. The player will be  matched against an AI. The game randomly decides who goes first, the 
			 player and the AI will then play the game out, until either side wins, or the game ends in a tie. After that the player's new score will be calculated, 
			 and the player will be asked to choose to play again or to leave. If the player chooses to leave, the game will end.
 */

public class ConnectFourAI {

	// Pair class used to store the column numbers and their corresponding heuristic
	// board values.
	public static class Pair implements Comparable<Pair> {
		int column, val;

		/**
		 * Name: Pair
		 * Description: Constructor for the Pair class
		 * @param column - int value for the column number
		 * @param val - int value for the value that results by placing a move in the column
		 */
		public Pair(int column, int val) {
			this.column = column;
			this.val = val;
		}

		public int compareTo(Pair other) { // compareTo to allow the priority queue to sort the data based on their boardScore at searching depth of 1
			if (val > other.val) { // This is to make the alpha beta pruning more efficient
				return 1;
			} else {
				return -1;
			}
		}
	}

	// initialize variables
	static Scanner sc = new Scanner(System.in);
	static char[][] board = new char[6][7];
	static String usernameInput;
	static final int PLAYER_PIECE = 1;
	static final int AI_PIECE = 0;
	static final int ROW = 6;
	static final int COL = 7;
	static int firstTurn; 			// who gets to go first (0 = AI, 1 = player), used in boardScore()
	static Map<String, Player> accounts = new HashMap<>(); // map to store the accounts

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// start of the game
		gameStart();
		String continueDecision = "";
		do {
			scoreOutput();
			System.out.println();
			System.out.println("Starting the game...");

			// filling the board/resetting the board
			for (int i = 0; i < ROW; i++) {
				Arrays.fill(board[i], '*');
			}

			// print out the initial board
			printBoard(board);

			firstTurn = (int) (Math.random() * 2);
			int turn = firstTurn;
			do {
				if (turn == 0) { // AI's turn
					// gets the column and row
					System.out.println("The AI is making a move...");
					int c = miniMax(18, board, Integer.MIN_VALUE, Integer.MAX_VALUE, true).column; // takes the column
																									// that generates
																									// the best score
					int r = nextOpenRow(board, c);
					dropPiece(board, r, c, AI_PIECE);
					printBoard(board);
					turn = 1; 					// Player's turn next
				} 
				else if (turn == 1) { 		// Player's turn, else if to check isTerminalNode() every time a move is placed)
					int playerChoice = -1;
					boolean flag = false;

					// user prompt
					do {
						flag = false;
						try {
							System.out.print("Please enter your choice of column to drop your piece at(1-7): ");
							playerChoice = sc.nextInt() - 1;
						} catch (InputMismatchException e) { 	// not an integer
							System.out.println("Invalid input, please try again");
							System.out.println();
							sc.nextLine(); 						// clears the scanner
							flag = true;
							continue;
						}

						// out of range
						if (playerChoice < 0 || playerChoice > 6) {
							System.out.println("Input out of range, please enter again");
							System.out.println();
						}
						// column already filled
						else if (!availableNodes(board).contains(playerChoice)) {
							System.out.println("The selected column is already filled, please enter again");
							System.out.println();
							flag = true;
						}
					} while (playerChoice < 0 || playerChoice > 6 || flag);

					// get row value, drop the piece and print the board
					int r = nextOpenRow(board, playerChoice);
					dropPiece(board, r, playerChoice, PLAYER_PIECE);
					printBoard(board);
					turn = 0; 					// AI's turn next
				}
			} while (!isTerminalNode(board)); 	// only ends when the game ends

			// End game
			if (winBoard(board, AI_PIECE)) {
				System.out.println("The AI won");
				accounts.get(usernameInput).losses++;
			} else if (winBoard(board, PLAYER_PIECE)) {
				System.out.println("The player won");
				accounts.get(usernameInput).wins++;
			} else {
				System.out.println("Tie");
				accounts.get(usernameInput).ties++;
			}

			// user prompt to ask for continue decision
			sc.nextLine();
			do {
				System.out.println();
				System.out.println("Would you like to play another game?");
				System.out.println("Enter \"YES\" to continue, enter \"NO\" to stop");
				continueDecision = sc.nextLine().toUpperCase();
			} while (!continueDecision.equals("YES") && !continueDecision.equals("NO"));
		} while (continueDecision.equals("YES"));

		// quit
		System.out.println();
		System.out.println("Thank you for playing Connect Four");
		System.out.println("Stay safe and have a nice day");

	}

	/**
	 * Name: miniMax
	 * Description: This method calculates and returns the best possible score and the column that generates that score given the searching depth
	 * @param depth - The current searching depth of the AI
	 * @param board - 2-D array for the current board
	 * @param alpha - Variable for alpha-beta pruning
	 * @param beta - Variable for alpha-beta pruning
	 * @param maximizingPlayer - Boolean value to indicate which player's turn it is (maximizing or minimizing player)
	 * @return Pair - returns the best pair that contains the best column and the best heuristic value of the board
	 */
	public static Pair miniMax(int depth, char[][] board, int alpha, int beta, boolean maximizingPlayer) {

		// base cases
		if (depth == 0 || isTerminalNode(board)) {
			Pair pair = new Pair(-1, boardScore(board)); 	// -1 is a place holder, it will never get used as it will get updated right after it is returned;
			return pair;
		}

		// getting all the available nodes
		LinkedList<Pair> tempList = pickBestMove(board, maximizingPlayer);
		PriorityQueue<Pair> queue;
		if (maximizingPlayer) {
			queue = new PriorityQueue<>(Collections.reverseOrder()); // decreasing order for the AI (highest value first)
			int count = tempList.size();
			for (int i = 0; i < count; i++) {
				queue.add(tempList.poll());
			}
		} else {
			queue = new PriorityQueue<>(); // increasing order for the player (lowest value first)
			int count = tempList.size();
			for (int i = 0; i < count; i++) {
				queue.add(tempList.poll());
			}
		}

		//is true, maximizing player's turn, (the AI)
		if (maximizingPlayer) {
			Pair pair = new Pair(-1, 0); 		// initialize the variables
			int value = Integer.MIN_VALUE;

			for (int i = 0; i < queue.size(); i++) { // loop through every available node
				Pair temp = queue.poll();
				int child = temp.column;

				// storing values into a temp new array
				char[][] boardCopy = new char[6][7];
				for (int row = 0; row < ROW; row++) {
					for (int col = 0; col < COL; col++) {
						boardCopy[row][col] = board[row][col];
					}
				}

				// recursively get all the possible values and return the highest possible value
				int r = nextOpenRow(boardCopy, child);
				dropPiece(boardCopy, r, child, AI_PIECE); 								// drop ai piece into the boardCopy
				int nodeValue = miniMax(depth - 1, boardCopy, alpha, beta, false).val; 	// minimizing player next turn, taking the value of the node
				if (nodeValue > value) {
					value = nodeValue;
					pair = new Pair(child, value); 			// updating pair to be returned
				}
				// alpha beta pruning (can be found on google)
				alpha = Math.max(value, alpha);
				if (alpha >= beta) {
					break;
				}
			}
			return pair;
		}
		// is false, minimizing player's turn, pretending to be the player
		else {
			Pair pair = new Pair(-1, 0); 
			int value = Integer.MAX_VALUE;

			for (int i = 0; i < queue.size(); i++) { 			// loop through every available node
				Pair temp = queue.poll();
				int child = temp.column;

				// storing values into a temp new array
				char[][] boardCopy = new char[6][7];
				for (int row = 0; row < ROW; row++) {
					for (int col = 0; col < COL; col++) {
						boardCopy[row][col] = board[row][col];
					}
				}

				// recursively get all the possible values and return the lowest possible value
				int r = nextOpenRow(boardCopy, child);
				dropPiece(boardCopy, r, child, PLAYER_PIECE); 			// drop player piece into boardCopy
				int nodeValue = miniMax(depth - 1, boardCopy, alpha, beta, true).val; // minimizing player next turn, taking the value of the node
				if (nodeValue < value) {
					value = nodeValue;
					pair = new Pair(child, value);
				}
				// alpha beta pruning (can be found on google)
				beta = Math.min(value, beta);
				if (alpha >= beta) {
					break;
				}
			}
			return pair;
		}
	}


	/**
	 * Name: gameStart
	 * Description: This method asks the users for their usernames (log in, create new account)...
	 */
	public static void gameStart() {

		usernameInput = "";

		// ask for player type and check for exceptions
		while (true) {

			System.out.println("=================================================================================");
			System.out.println("Please enter your username: ");
			usernameInput = sc.nextLine();

			if (accounts.containsKey(usernameInput)) { // if the account already exist
				System.out.println("Please enter your password: ");
				String passwordInput = sc.nextLine();

				if (accounts.get(usernameInput).login(passwordInput)) { // if password is correct
					System.out.println("Welcome back, " + usernameInput);
					break;
				}

				System.out.println("Incorrect username or password, please try again.\n");
			} else { // new account
				System.out.println(
						"This account is not in our files, please enter a password of your choice to create a new account: ");
				String passwordInput = sc.nextLine();

				accounts.put(usernameInput, new Player(usernameInput, passwordInput));

				System.out.println("\nAccount created!");
				break;
			}
		}
	}


	/**
	 * Name: scoreOutput
	 * Description: This method outputs the current score of the player (stored in the player's account)
	 */
	public static void scoreOutput() {
		// outputs the current score
		System.out.println();
		System.out.println();
		System.out.println(accounts.get(usernameInput));
	}


	/**
	 * Name: printBoard
	 * Description: This method outputs the current conditions of the board
	 * @param board - 2-D char array for the current board to be outputted
	 */
	public static void printBoard(char[][] board) {
		for (int row = 0; row < ROW; row++) {
			for (int col = 0; col < COL; col++) {
				System.out.print(board[row][col] + " ");
			}
			System.out.println();
		}
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println();
	}

	
	/**
	 * Name: winBoard
	 * Description: This method checks if the game has ended by either side winning
	 * @param board - 2-D char array to store the current board
	 * @param piece - int value to determine who the program is checking (player or AI)
	 * @return - boolean value to indicate whether a winning condition is reached or not
	 */
	public static boolean winBoard(char[][] board, int piece) {
		piece = piece + 48; 	// change to char value
		// horizontal
		for (int row = 0; row < ROW; row++) {
			for (int col = 0; col < COL - 3; col++) {
				if (board[row][col] == piece && board[row][col + 1] == piece && board[row][col + 2] == piece
						&& board[row][col + 3] == piece) {
					return true;
				}
			}
		}

		// vertical
		for (int col = 0; col < COL; col++) {
			for (int row = 0; row < ROW - 3; row++) {
				if (board[row][col] == piece && board[row + 1][col] == piece && board[row + 2][col] == piece
						&& board[row + 3][col] == piece) {
					return true;
				}
			}
		}

		// negatively sloped diagonals
		for (int row = 0; row < ROW - 3; row++) {
			for (int col = 0; col < COL - 3; col++) {
				if (board[row][col] == piece && board[row + 1][col + 1] == piece && board[row + 2][col + 2] == piece
						&& board[row + 3][col + 3] == piece) {
					return true;
				}
			}
		}

		// positively sloped diagonals
		for (int row = 3; row < ROW; row++) {
			for (int col = 0; col < COL - 3; col++) {
				if (board[row][col] == piece && board[row - 1][col + 1] == piece && board[row - 2][col + 2] == piece
						&& board[row - 3][col + 3] == piece) {
					return true;
				}
			}
		}

		return false;
	}


	/**
	 * Name: isTerminalNode
	 * Description: Checks if the game has ended by either side winning or by ties
	 * @param board - 2-D char array to store the current board
	 * @return - boolean value to indicate whether the game has ended or not
	 */
	public static boolean isTerminalNode(char[][] board) {
		return winBoard(board, PLAYER_PIECE) || winBoard(board, AI_PIECE) || availableNodes(board).size() == 0;
	}


	/**
	 * Name: availableNodes
	 * Description: This method checks for the available columns on the board where a move can be placed
	 * @param board - 2-D char array to store the current board
	 * @return - LinkedList<Integer> to store the available nodes
	 */
	public static LinkedList<Integer> availableNodes(char[][] board) {
		LinkedList<Integer> queue = new LinkedList<>();
		for (int i = 0; i < COL; i++) {
			if (board[0][i] == '*') {
				queue.add(i);
			}
		}
		return queue;
	}


	/**
	 * Name: nextOpenRow
	 * Description: This method finds the next open row in the selected column (the row where the piece will fall to when dropped from this column)
	 * @param board - 2-D char array to store the current board
	 * @param c - column number
	 * @return - int value indicating the row number
	 */
	public static int nextOpenRow(char[][] board, int c) {
		for (int i = ROW - 1; i >= 0; i--) { 			// starts from the bottom, searches up
			if (board[i][c] == '*') {
				return i;
			}
		}

		// error (will never encounter due to the way this method is called)
		return -1;
	}


	/**
	 * Name: dropPiece
	 * Description: This method updates the 2-D board array with the new piece
	 * @param board - 2-D char array to store the current board
	 * @param r - The row number at which the piece will be dropped to
	 * @param c - The column number at which the piece will be dropped to
	 * @param piece - The player's piece or the AI's piece
	 */
	public static void dropPiece(char[][] board, int r, int c, int piece) {
		board[r][c] = (char) (piece + 48);
	}


	/**
	 * Name: boardScore
	 * Description: This method calculates and returns the total score of the board after evaluating the board
	 * @param board - 2-D char array to store the current board
	 * @return - int value to indicate the heuristic score of the board
	 */
	public static int boardScore(char[][] board) {
		int totalScore = 0;
		int aiPieceCount = 0;
		int playerPieceCount = 0;
		int emptyCount = 0;

		// bonus marks for center column
		for (int row = 0; row < ROW; row++) {
			if (board[row][3] == (char) (AI_PIECE + 48)) {
				totalScore += 20; 			// 20 points for every piece at the center column
			}
			if (board[row][3] == (char) (PLAYER_PIECE + 48)) {
				totalScore -= 16; 			// take away 16 points for every enemy (player) piece at the center column
			}
		}

		// check for row wins and threats
		for (int row = 0; row < ROW; row++) {
			for (int col = 0; col < COL - 3; col++) {
				int c = -1; 			// gets the column number for the empty piece (where the threat is at)
				int r = -1; 			// gets the row number for the empty piece
				
				// checking the window (size of 4)
				for (int i = 0; i < 4; i++) {
					if (board[row][col + i] == (char) (AI_PIECE + 48)) {
						aiPieceCount++;
					} else if (board[row][col + i] == (char) (PLAYER_PIECE + 48)) {
						playerPieceCount++;
					} else {
						emptyCount++;
						r = row;
						c = col + i;
					}
				}
				
				// evaluate the window that the program currently checking
				if (aiPieceCount == 4) {
					return Integer.MAX_VALUE - 1;
				}
				if (playerPieceCount == 4) {
					return Integer.MIN_VALUE + 1;
				}
				totalScore = evaluateWindow(totalScore, r, c, aiPieceCount, emptyCount, board, AI_PIECE);
				totalScore = evaluateWindow(totalScore, r, c, playerPieceCount, emptyCount, board, PLAYER_PIECE);

				// reset the values
				aiPieceCount = 0;
				playerPieceCount = 0;
				emptyCount = 0;
			}
		}

		// check for column wins and threats
		for (int col = 0; col < COL; col++) {
			for (int row = 0; row < ROW - 3; row++) {
				
				// checking the window (size of 4)
				for (int i = 0; i < 4; i++) {
					if (board[row + i][col] == (char) (AI_PIECE + 48)) {
						aiPieceCount++;
					} else if (board[row + i][col] == (char) (PLAYER_PIECE + 48)) {
						playerPieceCount++;
					} else {
						emptyCount++;
					}
				}
				
				// evaluate the window that the program currently checking
				if (aiPieceCount == 4) {
					return Integer.MAX_VALUE - 1;
				}
				if (playerPieceCount == 4) {
					return Integer.MIN_VALUE + 1;
				}
				if (aiPieceCount == 3 && emptyCount == 1) {
					totalScore += 70;
				}
				if (playerPieceCount == 3 && emptyCount == 1) {
					totalScore -= 70;
				}

				// reset values
				aiPieceCount = 0;
				playerPieceCount = 0;
				emptyCount = 0;
			}
		}

		// check for negatively sloped diagonals
		for (int row = 0; row < ROW - 3; row++) {
			for (int col = 0; col < COL - 3; col++) {
				int c = -1; 			// gets the column number for the empty piece (where the threat is at)
				int r = -1; 			// gets the row number for the empty piece
				
				for (int i = 0; i < 4; i++) {
					if (board[row + i][col + i] == (char) (AI_PIECE + 48)) {
						aiPieceCount++;
					} else if (board[row + i][col + i] == (char) (PLAYER_PIECE + 48)) {
						playerPieceCount++;
					} else {
						emptyCount++;
						c = col + i;
						r = row + i;
					}
				}
				
				// evaluate the window that the program currently checking
				if (aiPieceCount == 4) {
					return Integer.MAX_VALUE - 1;
				}
				if (playerPieceCount == 4) {
					return Integer.MIN_VALUE + 1;
				}
				totalScore = evaluateWindow(totalScore, r, c, aiPieceCount, emptyCount, board, AI_PIECE);
				totalScore = evaluateWindow(totalScore, r, c, playerPieceCount, emptyCount, board, PLAYER_PIECE);

				// reset values
				aiPieceCount = 0;
				playerPieceCount = 0;
				emptyCount = 0;
			}
		}

		// positively sloped diagonals
		for (int row = 3; row < ROW; row++) {
			for (int col = 0; col < COL - 3; col++) {
				int c = -1; 			// gets the column number for the empty piece (where the threat is at)
				int r = -1; 			// gets the row number for the empty piece
				
				for (int i = 0; i < 4; i++) {
					if (board[row - i][col + i] == (char) (AI_PIECE + 48)) {
						aiPieceCount++;
					} else if (board[row - i][col + i] == (char) (PLAYER_PIECE + 48)) {
						playerPieceCount++;
					} else {
						emptyCount++;
						c = col + i;
						r = row - i;
					}
				}
				
				// evaluate the window that the program currently checking
				if (aiPieceCount == 4) {
					return Integer.MAX_VALUE - 1;
				}
				if (playerPieceCount == 4) {
					return Integer.MIN_VALUE + 1;
				}
				totalScore = evaluateWindow(totalScore, r, c, aiPieceCount, emptyCount, board, AI_PIECE);
				totalScore = evaluateWindow(totalScore, r, c, playerPieceCount, emptyCount, board, PLAYER_PIECE);

				// reset values
				aiPieceCount = 0;
				playerPieceCount = 0;
				emptyCount = 0;
			}
		}

		// return the total score on the board
		return totalScore;
	}

	
	/**
	 * Name: evaluateWindow
	 * Description: This method calculates and returns the current total score of the board after evaluating this window
	 * @param totalScore - int value to store the total score of the board
	 * @param r - row number
	 * @param c - column number
	 * @param pieceCount - number of pieces
	 * @param emptyCount - number of empty spaces
	 * @param board - 2-D char array to store the current board
	 * @param piece - the player's piece or the AI's piece
	 * @return - int value of the total score of the board
	 */
	public static int evaluateWindow(int totalScore, int r, int c, int pieceCount, int emptyCount, char[][] board,
			int piece) {
		int subtotal = 0;
		if (pieceCount == 3 && emptyCount == 1) {
			int openR = nextOpenRow(board, c);
			subtotal = subtotal + 70 - (10 * (openR - r)); // -10 points for every empty space between the available row and the threat row.

			// odd-even strategy of the game
			// If checking AI_PIECE
			if (piece == AI_PIECE) {
				if (r % 2 == 0 && firstTurn == 1) { // If the player goes first this game, AI wants its threats to be at even rows
					subtotal += 40;
				} 
				else if (r % 2 != 0 && firstTurn == 0) { // If the AI goes first this game, AI wants its threats to be at odd rows
					subtotal += 40;
				}
			}
			// If checking PLAYER_PIECE
			else {
				if (r % 2 != 0 && firstTurn == 1) { // If the player goes first this game, player wants their threats to be at odd rows
					subtotal += 40;
				} 
				else if (r % 2 == 0 && firstTurn == 0) { // If the AI goes first this game, AI wants its threats to be at odd rows
					subtotal += 40;
				}
			}

		}

		if (piece == AI_PIECE) { 	// the AI
			totalScore += subtotal;
		} 
		else { 						// the player
			totalScore -= subtotal;
		}

		return totalScore;
	}

	/*
	 * LinkedList<Pair> pickBestMove () returns LinkedList<Pair> - Returns the
	 * possible moves with their heuristic values char[][] board - This parameter
	 * tells the program which board to check boolean maximizingPlayer - This
	 * parameter tells the program whose turn it is This method calculates and
	 * returns the all the current possible columns to drop a piece and with their
	 * heuristic board values
	 */
	/**
	 * Name: pickBestMove
	 * Description: This method calculates and returns all the current possible columns to drop a piece and with their heuristic board values
	 * @param board - 2-D char array to store the current board
	 * @param maximizingPlayer - boolean value to indicate whether it is the maximizingPlayer or the minimizingPlayer that the program is calculating for
	 * @return - LinkedList<Pair> for the possible moves with their heuristic values
	 */
	public static LinkedList<Pair> pickBestMove(char[][] board, boolean maximizingPlayer) {
		LinkedList<Pair> queue = new LinkedList<>();

		// AI, drop ai piece
		if (maximizingPlayer) {
			for (int i : availableNodes(board)) {
				int r = nextOpenRow(board, i);
				dropPiece(board, r, i, AI_PIECE);
				Pair pair = new Pair(i, boardScore(board));
				queue.add(pair);
				board[r][i] = '*';
			}
		}
		// PLAYER, drop player piece
		else {
			for (int i : availableNodes(board)) {
				int r = nextOpenRow(board, i);
				dropPiece(board, r, i, PLAYER_PIECE);
				Pair pair = new Pair(i, boardScore(board));
				queue.add(pair);
				board[r][i] = '*';
			}
		}

		return queue;
	}
}
