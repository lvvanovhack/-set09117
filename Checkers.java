import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;


/**
 * This panel lets two users play checkers against each other.
 * Red always starts the game.  If a player can jump an opponent's
 * piece, then the player must jump.  When a player can make no more
 * moves, the game ends.
 * 
 * The class has a main() routine that lets it be run as a stand-alone
 * application.  The application just opens a window that uses an object
 * of type Checkers as its content pane.
 */

//Last Edited by Ivanov N. on 10.2017
    class Checkers extends JPanel 
    {
	private static final long serialVersionUID = 1L;
    /**
    * Main routine makes it possible to run Checkers as a stand-alone
    * application.  Opens a window showing a Checkers panel; the program
    * ends when the user closes the window.
    */
	public static void main(String[] args) 
   {
      JFrame window = new JFrame("Checkers");
      Checkers content = new Checkers();
      window.setContentPane(content);
      window.pack();
      Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
      window.setLocation( (screensize.width - window.getWidth())/2,
            (screensize.height - window.getHeight())/2 );
      window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      window.setResizable(true);  
      window.setVisible(true);
   }     
	private JButton undoButton;     // Button for undoing an action.
	private JButton redoButton;     //Button for redoing an action.
	private JButton newGameButton;  // Button for starting a new game.
	private JButton aiButton;             //Button for the AI turns
	private JButton resignButton;   // Button that a player can use to end 
                                   // the game by resigning.
	private JLabel msgmoves;        // Label showing last move made.
	private JLabel userMsg;         // Label for displaying messages to the user.
    
	Movement m;
	static Movement[] moveArray;
	private static Stack<Movement> rStack;
	private static Stack<Movement> lStack;
	private static Stack<Movement> uStack;
   /**
    * The constructor creates the Board (which in turn creates and manages
    * the buttons and message label), adds all the components, and sets
    * the bounds of the components.  A null layout is used.  (This is
    * the only thing that is done in the main Checkers class.)
    */
   public Checkers() 
   {     
      setLayout(null);  // I will do the layout myself.
      setPreferredSize(new Dimension(340,240));   
      setBackground(new Color(37,37,37));  // Dark grey background.
      Board board = new Board();  // Board Constructor. Also creates labels and buttons.
      add(board);
      add(newGameButton);
      add(resignButton);
      add(aiButton);
      add(undoButton);
      add(redoButton);
      add(userMsg);
      add(msgmoves);    
      // Setting the actual size with setBounds() method
      board.setBounds(20,20,164,164); 
      newGameButton.setBounds(210, 20, 120, 30);
      resignButton.setBounds(210, 80, 120, 30);
      aiButton.setBounds(50, 240, 120, 30);
      undoButton.setBounds(210,140,120,30);
      redoButton.setBounds(210,170,120,30);
      userMsg.setBounds(0, 200, 350, 30);
      msgmoves.setBounds(50,130,130,130);
      
   } // end constructor 
   //Temporary nested classes, need to be fixed
   /**
    * A CheckersMove object represents a move in the game of Checkers.
    * It holds the row and column of the piece that is to be moved
    * and the row and column.  
    */
   private static class Movement
   {
      int fromRow, fromCol;  // Position of piece to be moved.
      int toRow, toCol;      // Square it is to move to.
      //Constructor
      Movement(int r1, int c1, int r2, int c2) 
      {
         fromRow = r1;
         fromCol = c1;
         toRow = r2;
         toCol = c2;
      } // end constructor
      boolean jumping() 
      {
             // Test whether this move is a jump.
         return (fromRow - toRow == 2 || fromRow - toRow == -2);
      }
   }  // end
    
   /**
    * This panel displays a 160-by-160 checkerboard pattern with
    * a 2-pixel black border.  It is assumed that the size of the
    * panel is set to exactly 164-by-164 pixels.  This class does
    * the work of letting the users play checkers, and it displays
    * the checkerboard.
    */
   private class Board extends JPanel implements ActionListener, MouseListener 
   {
	private static final long serialVersionUID = 9170179771007341643L;
    private boolean gameRun; // Is a game currently in progress?
	Data board;  
	//Array with the moves that are legal to be made.
    Movement[] legalMoves;
      /**
       * The next three variables are valid only when the game is in progress.
       * Assuming the turn and if player = RED or BLACK
       */
      int currPlayer;      
      /**
       * Containing the piece to move given the row and column containing
       * in that piece. If there is no a a piece selected, then selectedRow -1
       */
      int selectedRow, selectedCol;  
      /**
       * Constructor calling and creating existing actionlisteners.  
       * Create the board and start the first game.
       */
      Board() 
      {
    	 rStack = new Stack<Movement>();
    	 uStack = new Stack<Movement>();
         setBackground(Color.BLACK);
         addMouseListener(this);
         resignButton = new JButton("Resign");
         resignButton.addActionListener(this);
         newGameButton = new JButton("New Game");
         newGameButton.addActionListener(this);
         aiButton = new JButton("Make AI Move");
         aiButton.addActionListener(this);
         undoButton = new JButton("Undo");
         undoButton.addActionListener(this);
         redoButton = new JButton("Redo");
         redoButton.addActionListener(this);
         userMsg = new JLabel("",JLabel.CENTER);
         userMsg.setForeground(Color.green);
         msgmoves = new JLabel("", JLabel.CENTER);
         msgmoves.setForeground(Color.magenta);
         board = new Data();
      } 
      
      //Respond to user's click on a button
      public void actionPerformed(ActionEvent event) 
      {
         Object src = event.getSource();
         if (src == newGameButton)
            runGame();
         else if (src == resignButton)
            resign();
         else if( src == undoButton && lStack.empty() == false)
         {
        	uStack.push(m);
        	Movement mov = uStack.peek();
        	rStack.push(mov);
        	uStack.pop();
        	makeMove(mov);
         }
         else if( src == redoButton)
         {
        	uStack.push(m);
         	Movement mov = rStack.peek();
         	uStack.push(mov);
         	rStack.pop();
         	makeMove(mov);
         }
         else if(src == aiButton)
         {
        	
        	Movement aiMove = lStack.peek();
        	uStack.push(aiMove);
        	aiMove = uStack.peek();
        	uStack.pop();
        	makeMove(aiMove);
         }
      }
      //Start a new game.
      void runGame() 
      {
         if (gameRun == true) 
         {
            userMsg.setText("You have not finished your last game!");
            return;
         }
         board.prepareGame();   // Set up the pieces.
         currPlayer = Data.WHITE;   // WHITE always makes the first move       
         legalMoves = board.getLegalMoves(Data.WHITE);  // get the moves   
         selectedRow = -1;   // By default at start of each game.
         userMsg.setText("White:  Make your move.");
         gameRun = true;
         newGameButton.setEnabled(false);
         resignButton.setEnabled(true);
         repaint();
      }

      // Current player resigns.  Game ends.  Opponent wins.
      void resign() 
      {
         if (gameRun == false) 
         {  
            userMsg.setText("There is no game in progress!");
            return;
         }
         if (currPlayer == Data.WHITE)
            gameOver("WHITE resigns.  BLACK wins.");
         else
            gameOver("BLACK resigns.  WHITE wins.");
      }
      /**
       * The game ends.  The parameter, str, is displayed as a message
       * to the user.  The states of the buttons are adjusted so players
       * can start a new game.  This method is called when the game
       * ends at any point in this class.
       */
      void gameOver(String str) 
      {
         userMsg.setText(str);
         newGameButton.setEnabled(true);
         resignButton.setEnabled(false);
         gameRun = false;
      } 
      /**
       * This is called by mousePressed() when a player clicks on the
       * square in the specified row and column.
       */
      void clickMove(int row, int col) 
      { 
         for (int i = 0; i < legalMoves.length; i++)
            if (legalMoves[i].fromRow == row && legalMoves[i].fromCol == col) 
            {
               selectedRow = row;
               selectedCol = col;
               if (currPlayer == Data.WHITE)
               {
               userMsg.setText("WHITE:  Make your move.");
               msgmoves.setText("WHITE move:" + " " + selectedRow + " " + selectedCol);
               }
               else
               {
               userMsg.setText("BLACK:  Make your move.");
               msgmoves.setText("Black move:" + " " + selectedRow + " " + selectedCol);  
               }
               repaint();
               return;
            }     
         /* If no piece has been selected to be moved, the user must first
          select a piece.  Show an error message and return. */   
         if (selectedRow < 0) 
         {
            userMsg.setText("Click over a piece");
            return;
         }
         /* If the user clicked on a square where the selected piece can be
          legally moved, move and return. */ 
         for (int i = 0; i < legalMoves.length; i++)
            if (legalMoves[i].fromRow == selectedRow && legalMoves[i].fromCol == selectedCol
                  && legalMoves[i].toRow == row && legalMoves[i].toCol == col) 
            {
               makeMove(legalMoves[i]);
               return;
            }
         /* If we get to this point, there is a piece selected, and the square where
          the user just clicked is not one where that piece can be legally moved.
          Show an error message. */
         userMsg.setText("Click over a piece to move");        
      }  // end 
      
      /**
       * This is called when the current player has chosen the specified
       * move.  Make the move, and then either end or continue the game.
       */
      void makeMove(Movement move) 
      {	  
    	 m=move;
         board.makeMove(move);      
         /* If the move was a jump, it's possible that the player has another
          jump.  Check for legal jumps starting from the square that the player
          just moved to.  If there are any, the player must jump.  The same
          player continues moving.
          */
         if (move.jumping()) 
         {
            legalMoves = board.getLegalJumpsFrom(currPlayer,move.toRow,move.toCol);
            if (legalMoves != null) {
               if (currPlayer == Data.WHITE)
                  userMsg.setText("WHITE: Continue jumping.");
               else
                  userMsg.setText("BLACK: Continue jumping.");
               
               selectedRow = move.toRow; 
               selectedCol = move.toCol;
               repaint();
               return;
            }
         }       
         /* The current player's turn is ended, so change to the other player.
          Get that player's legal moves.  If the player has no legal moves,
          then the game ends. */
         if (currPlayer == Data.WHITE) 
         {
            currPlayer = Data.BLACK;
            legalMoves = board.getLegalMoves(currPlayer);
            if (legalMoves == null)
               gameOver("BLACK has no moves.  WHITE wins.");
            else if (legalMoves[0].jumping())
               userMsg.setText("BLACK:  Click on piece to jump");
            else
               userMsg.setText("BLACK:  Make your move.");
         }
         else 
         {
            currPlayer = Data.WHITE;
            legalMoves = board.getLegalMoves(currPlayer);
            if (legalMoves == null)
               gameOver("WHITE has no moves.  BLACK wins.");
            else if (legalMoves[0].jumping())
               userMsg.setText("WHITE: Click on piece to jump");
            else
               userMsg.setText("WHITE:  Make your move.");
         }      
         /**
          *  Set selectedRow = -1 to record that the player has not yet selected
          a  piece to move. 
          */
         selectedRow = -1;
         /**
          *  As a courtesy to the user, if all legal moves use the same piece, then 
          *  select that piece automatically so the user won't have to click on it
          */
         if (legalMoves != null) {
            boolean sameStartSquare = true;
            for (int i = 1; i < legalMoves.length; i++)
               if (legalMoves[i].fromRow != legalMoves[0].fromRow
                     || legalMoves[i].fromCol != legalMoves[0].fromCol) {
                  sameStartSquare = false;
                  break;
               }
            if (sameStartSquare) {
               selectedRow = legalMoves[0].fromRow;
               selectedCol = legalMoves[0].fromCol;
            }
         }
         // Make sure the board is redrawn in its new state. 
         repaint();   
      }  // end   
      //Draw a checkerboard in gray and lightGray.
      public void paint(Graphics g) {
         //Border
    	  g.setColor(Color.black);
          g.drawRect(0,0,getSize().width-1,getSize().height-1);
          g.drawRect(1,1,getSize().width-3,getSize().height-3); 
         //Checkers and squares
          for (int row = 0; row < 8; row++) {
              for (int col = 0; col < 8; col++) {
                 if ( row % 2 == col % 2 )
                    g.setColor(Color.LIGHT_GRAY);
                 else
                     g.setColor(Color.GRAY);
                  g.fillRect(2 + col*20, 2 + row*20, 20, 20);
                  switch (board.piece(row,col)) {
                  case Data.WHITE:
                     g.setColor(Color.WHITE);
                     g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                     break;
                  case Data.BLACK:
                     g.setColor(Color.BLACK);
                     g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                     break;
                  case Data.WHITE_KING:
                     g.setColor(Color.WHITE);
                     g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                     g.setColor(Color.BLACK);
                     g.drawString("K", 7 + col*20, 16 + row*20);
                     break;
                  case Data.BLACK_KING:
                     g.setColor(Color.BLACK);
                     g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                     g.setColor(Color.WHITE);
                     g.drawString("K", 7 + col*20, 16 + row*20);
                     break;
               }
            }
         }   
         //Legal moves are highlighted/         
         if (gameRun) 
         {
            g.setColor(Color.green);
            for (int i = 0; i < legalMoves.length; i++) {
               g.drawRect(2 + legalMoves[i].fromCol*20, 2 + legalMoves[i].fromRow*20, 19, 19);
               g.drawRect(3 + legalMoves[i].fromCol*20, 3 + legalMoves[i].fromRow*20, 17, 17);
            }
               /* If a piece is selected for moving (i.e. if selectedRow >= 0), then
                draw a 2-pixel white border around that piece and draw green borders 
                around each square that that piece can be moved to. */
            if (selectedRow >= 0) {
               g.setColor(Color.white);
               g.drawRect(2 + selectedCol*20, 2 + selectedRow*20, 19, 19);
               g.drawRect(3 + selectedCol*20, 3 + selectedRow*20, 17, 17);
               g.setColor(Color.green);
               for (int i = 0; i < legalMoves.length; i++) {
                  if (legalMoves[i].fromCol == selectedCol && legalMoves[i].fromRow == selectedRow) {
                     g.drawRect(2 + legalMoves[i].toCol*20, 2 + legalMoves[i].toRow*20, 19, 19);
                     g.drawRect(3 + legalMoves[i].toCol*20, 3 + legalMoves[i].toRow*20, 17, 17);
                  }
               }
            }
         }
      }  // end 
      /**
       * Respond to a user click on the board.  If no game is in progress, show 
       * an error message.  Otherwise, find the row and column that the user 
       * clicked and call doClickSquare() to handle it.
       */
      public void mousePressed(MouseEvent evt) 
      {
         if (gameRun == false)
            userMsg.setText("Click \"New Game\" to start over again.");
         else {
            int col = (evt.getX() - 2) / 20;
            int row = (evt.getY() - 2) / 20;
            if (col >= 0 && col < 8 && row >= 0 && row < 8)
               clickMove(row,col);
         }
      }
      public void mouseReleased(MouseEvent evt) { }
      public void mouseClicked(MouseEvent evt) { }
      public void mouseEntered(MouseEvent evt) { }
      public void mouseExited(MouseEvent evt) { }
   }  // end
   /**
    * An object of this class holds data about a game of checkers.
    * It knows what kind of piece is on each square of the board.
    * Note that RED moves "up" the board (i.e. row number decreases)
    * while BLACK moves "down" the board (i.e. row number increases).
    * Methods are provided to return lists of available legal moves.
    */
   private static class Data 
   {
      //  The following constants represent a square piece
      static final int
                EMPTY = 0,
                WHITE = 1,
                WHITE_KING = 2,
                BLACK = 3,
                BLACK_KING = 4;
      
      
      int[][] board;  // board[r][c] is the contents of row r, column c.  
       // Constructor.  Create the board and set it up for a new game.
      Data() 
      {
         board = new int[8][8];
         prepareGame();
      }
      /**
       * Set up the board with checkers in position when game started
       * Note that checkers can only be found in squares
       * that satisfy  row % 2 == col % 2.  At the start of the game,
       * all such squares in the first three rows contain black squares
       * and all such squares in the last three rows contain red squares.
       */
      void prepareGame() 
      {
         for (int row = 0; row < 8; row++) 
         {
            for (int col = 0; col < 8; col++) 
            {
               if ( row % 2 == col % 2 ) 
               {
                  if (row < 3)
                     board[row][col] = BLACK;
                  else if (row > 4)
                     board[row][col] = WHITE;
                  else
                     board[row][col] = EMPTY;
               }
               else 
               {
                  board[row][col] = EMPTY;
               }
            }
         }
      } //end
       // Return the contents of the square in the specified row and column.
      int piece(int row, int col) 
      {
         return board[row][col];
      }
      void makeMove(Movement move) 
      {
         makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol);
      } 
      /**
       * Make the move from (fromRow,fromCol) to (toRow,toCol).  It is
       * assumed that this move is legal.  If the move is a jump, the
       * jumped piece is removed from the board.  If a piece moves to
       * the last row on the opponent's side of the board, the 
       * piece becomes a king.
       */
      void makeMove(int fromRow, int fromCol, int toRow, int toCol) 
      {
         board[toRow][toCol] = board[fromRow][fromCol];
         board[fromRow][fromCol] = EMPTY;
         // If jumping then remove the jumped piece from the board.
         if (fromRow - toRow == 2 || fromRow - toRow == -2) 
         {    
            int jumpRow = (fromRow + toRow) / 2;  // Row of the jumped piece.
            int jumpCol = (fromCol + toCol) / 2;  // Column of the jumped piece.
            board[jumpRow][jumpCol] = EMPTY;
         }
         if (toRow == 0 && board[toRow][toCol] == WHITE)
            board[toRow][toCol] = WHITE_KING;
         if (toRow == 7 && board[toRow][toCol] == BLACK)
            board[toRow][toCol] = BLACK_KING;
      }
      
     
      /**
       * If there are any legal moves for each player
      */
      Movement[] getLegalMoves(int player) 
      {  
    	  lStack = new Stack<Movement>();  //Storing moves in a stack that is used later for redo/undo.
         if (player != WHITE && player != BLACK)
            return null;
         int playerKing;  
         if (player == WHITE)
            playerKing = WHITE_KING;
         else
            playerKing = BLACK_KING;  
         /**
          *  First, check for any possible jumps.  Look at each square on the board 
          *  If that square contains one of the player's pieces, look at a possible
          *  jump in each of the four directions from that square.  If there is 
          *  a legal jumping it's put it in the stack.
          */    
         for (int row = 0; row < 8; row++) 
         {
            for (int col = 0; col < 8; col++) 
            {
               if (board[row][col] == player || board[row][col] == playerKing) 
               {
                  if (canJump(player, row, col, row+1, col+1, row+2, col+2))
                     lStack.push(new Movement(row, col, row+2, col+2));
                  if (canJump(player, row, col, row-1, col+1, row-2, col+2))
                     lStack.push(new Movement(row, col, row-2, col+2));
                  if (canJump(player, row, col, row+1, col-1, row+2, col-2))
                     lStack.push(new Movement(row, col, row+2, col-2));
                  if (canJump(player, row, col, row-1, col-1, row-2, col-2))
                     lStack.push(new Movement(row, col, row-2, col-2));
               }
               
            }
         }  
         /*  If any jump moves were found, then the user must jump, so we don't 
          add any regular moves.  However, if no jumps were found, check for
          any legal regular moves.  Look at each square on the board.
          If that square contains one of the player's pieces, look at a possible
          move in each of the four directions from that square.  If there is 
          a legal move in that direction.  */
         if (lStack.size() == 00) 
         {
            for (int row = 0; row < 8; row++) 
            {
               for (int col = 0; col < 8; col++) 
               {
                  if (board[row][col] == player || board[row][col] == playerKing)
                  {
                     if (canMove(player,row,col,row+1,col+1))
                        lStack.push(new Movement(row, col, row+1, col+1));
                     if (canMove(player,row,col,row-1,col+1))
                        lStack.push(new Movement(row, col, row-1, col+1));
                     if (canMove(player,row,col,row+1,col-1))
                        lStack.push(new Movement(row, col, row+1, col-1));
                     if (canMove(player,row,col,row-1,col-1))
                        lStack.push(new Movement(row, col, row-1, col-1));
                  }
               }
               
            }
         }
         /* If no legal moves have been found, return null.  Otherwise, create
          an array just big enough to hold all the legal moves, copy the
          legal moves from the stack into the array, and return the array. */
         if (lStack.size() == 0)
            return null;
         else 
         {
             moveArray = new Movement[lStack.size()];
            for (int i = 0; i < lStack.size(); i++)
               moveArray[i] = lStack.get(i);
            
            return moveArray;
         } 
      }  // end 
      /**
       * Return a list of the legal jumps that the specified player can
       * make starting from the specified row and column.  If no such
       * jumps are possible, null is returned.
       */
      Movement[] getLegalJumpsFrom(int player, int row, int col) {
         if (player != WHITE && player != BLACK)
            return null;
         int playerKing;  // The constant representing a King belonging to player.
         if (player == WHITE)
            playerKing = WHITE_KING;
         else
            playerKing = BLACK_KING;
         // The legal jumps will be stored in this list.
         ArrayList<Movement> moves = new ArrayList<Movement>();  

         if (board[row][col] == player || board[row][col] == playerKing) 
         {
            if (canJump(player, row, col, row+1, col+1, row+2, col+2))
               moves.add(new Movement(row, col, row+2, col+2));   
            if (canJump(player, row, col, row-1, col+1, row-2, col+2))
               moves.add(new Movement(row, col, row-2, col+2));
            if (canJump(player, row, col, row+1, col-1, row+2, col-2))
               moves.add(new Movement(row, col, row+2, col-2));
            if (canJump(player, row, col, row-1, col-1, row-2, col-2))
               moves.add(new Movement(row, col, row-2, col-2));
         }
         if (moves.size() == 0)
            return null;
         else 
         {
            Movement[] moveArray = new Movement[moves.size()];
            for (int i = 0; i < moves.size(); i++)
               moveArray[i] = moves.get(i);
            return moveArray;
         }
      }  // end 
      /**
       * This is called by the two previous methods to check whether the
       * player can legally jump from (r1,c1) to (r3,c3).  It is assumed
       * that the player has a piece at (r1,c1), that (r3,c3) is a position
       * that is 2 rows and 2 columns distant from (r1,c1) and that 
       * (r2,c2) is the square between (r1,c1) and (r3,c3).
       */
      private boolean canJump(int player, int r1, int c1, int r2, int c2, int r3, int c3) 
      {      
         if (r3 < 0 || r3 >= 8 || c3 < 0 || c3 >= 8)
            return false;  
         // (r3,c3) is off the board.
         if (board[r3][c3] != EMPTY)
            return false;  
         // (r3,c3) already contains a piece.
         if (player == WHITE) 
         {
            if (board[r1][c1] == WHITE && r3 > r1)
               return false;  
            // Only whites can move
            if (board[r2][c2] != BLACK && board[r2][c2] != BLACK_KING)
               return false; 
            // There is no black piece to jump.
            return true;
         }
         else 
         {
            if (board[r1][c1] == BLACK && r3 < r1)
               return false;  
            // Regular black piece can only move downn.
            if (board[r2][c2] != WHITE && board[r2][c2] != WHITE_KING)
               return false;  
            // There is no white piece to jump.
            return true;  
         }
         
      }  // end      
      /**
       * This is called to determine whether the player can legally move.
       *  It is assumed that (r1,r2) contains one of the player's pieces and
       * that (r2,c2) is a neighboring square.
       */
      private boolean canMove(int player, int r1, int c1, int r2, int c2) 
      {
         if (r2 < 0 || r2 >= 8 || c2 < 0 || c2 >= 8)
            return false;  
         // (r2,c2) is off the board.
         if (board[r2][c2] != EMPTY)
            return false;  
         // (r2,c2) already contains a piece.
         if (player == WHITE) {
            if (board[r1][c1] == WHITE && r2 > r1)
               return false; 
            // Regular white piece can only move down.
            return true; 
         }
         else 
         {
            if (board[r1][c1] == BLACK && r2 < r1) 
            {
               return false; 
            }
            return true;  
         }
         
      }  // end class       
   } // end Data class
} // end Checkers class
