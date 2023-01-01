# Connect Four Bot

## About
This is a text-based version of the game Connect Four. The player will be  matched against a bot. The game randomly decides who goes first. The player and the bot will then play the game out, until either side wins, or the game ends in a tie. After that the player's new score will be calculated, and the player will be asked to choose to play again or to leave. If the player chooses to continue, the above process will be repeated, otherwise, the game will end.


## How the Program Works
The program's AI is based on the minima algorithm, with the default searching depth set to 18. The algorithm recursively goes through all the possible boards of the game within 18 steps. Once the depth reaches 0, or when the game ends within the 18 steps, it returns the heuristic value of the board by calling the *`boardScore()`* function. The AI will then compare the scores from different paths, and choose the path that will lead to the highest board score. 


## Additional Optimization
Alpha-beta pruning was implemented to prune out the useless branches of the tree. A priority queue was also added to sort the available nodes based on their heuristic values returned by the *`pickBestMove()`* function, which runs the game with a searching depth of one. This feeds the minimax algorithm with more accurate nodes, or in other words, nodes in better order and thus makes the alpha-beta pruning more efficient. This method drastically improves the efficiencies of the code by over 87%. 


## Game Strategies that the Bot Adopts 
The game 


## Additional Notes
The searching depth of the AI can be changed, the difficulty of the AI directly relates to the searching depth (the higher the depth, the stronger the AI). The program will not function if the searching depth is set to 0, and if the searching depth is below 3, the AI will not be able to 
