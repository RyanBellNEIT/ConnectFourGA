import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;

public class DrawGrid
{

    //region Constructor + Main
    public DrawGrid()
    {
        JFrame frame = new JFrame("Connect Four");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(frame.getSize());
        frame.add(new MultiDraw(frame.getSize()));
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String... argv)
    {
        new DrawGrid();
    }
    //endregion

    @SuppressWarnings("SuspiciousListRemoveInLoop")
    public static class MultiDraw extends JPanel  implements MouseListener {

        private final int rows = 6;
        private final int cols = 7;
        private boolean winner = false;
        //Current Color
        private String cColor = "";
        //Controls whose turn it is in game
        private boolean redTurn = true;
        private boolean redStarted;
        private ArrayList<GAEnemy> yellowPieces = new ArrayList<>();
        private ArrayList<GAEnemy> redPieces = new ArrayList<>();

        //GA Variables
        private final Random random = new Random();
        private static final int yellowPopSize = 42;
        private static final int redPopSize = 42;

        private static final int yellowChromoLength = 21;
        private static final int redChromoLength = 21;

        private static final int yellowElitismAmount = 4;
        private static final int redElitismAmount = 4;

        private float yellowAvgFit = 0;
        private float redAvgFit = 0;

        //Holds the most fit red and yellow pieces
        private ArrayList<GAEnemy> yellowFittest = new ArrayList<>();
        private ArrayList<GAEnemy> redFittest = new ArrayList<>();

        //Holds population of red and yellow pieces
        private final ArrayList<GAEnemy> tYellowPop = new ArrayList<>();
        private final ArrayList<GAEnemy> tRedPop = new ArrayList<>();

        private int gameSpeed = 100;

        private int popNum;
        private int currentGen = 1;

        public Color[][] grid = new Color[rows][cols];
        //endregion

        public MultiDraw(Dimension dimension)
        {
            setSize(dimension);
            setPreferredSize(dimension);
            addMouseListener(this);
            //1. initialize array here
            for (int row = 0; row < grid.length; row++) {
                for (int col = 0; col < grid[0].length; col++)
                {
                    grid[row][col] = Color.white;
                }
            }
            redStarted = redTurn;
            initializeGA();
        }

        //region Main Update
        @SuppressWarnings("ForLoopReplaceableByForEach")
        @Override
        public void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D)g;
            Dimension d = getSize();
            g2.setColor(new Color(0, 0, 255));
            g2.fillRect(0,0,d.width,d.height);
            int startX = 0;
            int startY = 0;

            //region Yellow Turn
            if(!redTurn && !winner)
            {
                int clickedRow = 0;
                int clickedCol = yellowPieces.get(popNum).currentGene;

                clickedRow = findLowestWhite(clickedCol, false);

                grid[clickedRow][clickedCol] = Color.yellow;
                cColor =  "Yellow";

                updateGA();
                //redTurn = true;

                if(isWinner(clickedCol, clickedRow, grid[clickedRow][clickedCol]))
                {
                    winner=true;
                    popNum += 1;
                    if(popNum >= 42)
                    {
                        spawnNextGen();
                    }
                }

            }
            //endregion

            //region Red Turn
            if(redTurn && !winner)
            {
                int clickedRow = 0;
                int clickedCol = redPieces.get(popNum).currentGene;

                clickedRow = findLowestWhite(clickedCol, true);

                grid[clickedRow][clickedCol] = Color.red;
                cColor =  "Red";

                updateGA();
                //redTurn = false;

                if(isWinner(clickedCol,clickedRow, grid[clickedRow][clickedCol]))
                {
                    winner=true;
                    popNum += 1;

                    if(popNum >= 42)
                    {
                        spawnNextGen();
                    }
                }
            }
            //endregion

            //Makes playing grid
            for (int row = 0; row < grid.length; row++)
            {
                //region Variables
                int cellSize = 60;
                for (int col = 0; col < grid[0].length; col++)
                {

                    g2.setColor(grid[row][col]);
                    g2.fillRect(startX,startY, cellSize, cellSize);
                    g2.setColor(Color.black);
                    g2.drawRect(startX,startY, cellSize, cellSize);
                    startX += cellSize;
                }
                startY += cellSize;
                startX = 0;
            }

            g2.setColor(Color.white);

            //Calculates average fitness of red and yellow.
            avgFitness();

            g.drawString("Yellow Current Gene: " + yellowPieces.get(popNum).currentGene, 425, 60);
            g.drawString("Red Current Gene: "  + redPieces.get(popNum).currentGene, 425, 80);
            g.drawString("Current Population: "  + popNum, 425, 100);
            g.drawString("Current Generation: "  + currentGen, 425, 120);
            g.drawString("Yellow Current Fitness: " + yellowPieces.get(popNum).fitness, 425, 140);
            g.drawString("Red Current Fitness: " + redPieces.get(popNum).fitness, 425, 160);
            g.drawString("Yellow Average Fitness: " + Math.round(yellowAvgFit * 100.0) / 100.0, 425, 180);
            g.drawString("Red Average Fitness: " + Math.round(redAvgFit * 100.0) / 100.0, 425, 200);

            repaint();

            if(!winner)
            {
                redTurn = !redTurn;
                if(redTurn)
                {
                    g2.drawString("Red's Turn",450,20);
                }
                else {
                    g2.drawString("Yellow's Turn",450,20);
                }
            }
            else {
                g2.drawString("WINNER - "+ cColor,450,20);

                //If you want to stop game after a win for debug reasons, comment out reset call and uncomment try catch
                resetGame();
            }

            //COMMENT THIS OUT TO MAKE GAME PROGRESS WITHOUT A DELAY
            try {
                Thread.sleep(gameSpeed);
            }
            catch(InterruptedException e)
            {
                System.out.println(e.getMessage());
            }
        }
        //endregion

        //region GA Functions
        public void initializeGA()
        {
            for(int i = 0; i < yellowPopSize; i++)
            {
                yellowPieces.add(new GAEnemy());
                for (int j = 0; j < yellowChromoLength; j++)
                {
                    yellowPieces.get(i).AddGenes(newGene());
                }
            }

            for(int i = 0; i < redPopSize; i++)
            {
                redPieces.add(new GAEnemy());
                for (int j = 0; j < redChromoLength; j++)
                {
                    redPieces.get(i).AddGenes(newGene());
                }
            }
        }

        public int newGene()
        {
            return random.nextInt(7);
        }

        public void spawnNextGen()
        {
            if(winner && popNum == 42)
            {
                popNum = 0;
                winner = false;
                currentGen += 1;
                //If red started this generation, yellow will start the next generation.
                redTurn = !redStarted;
                redStarted = redTurn;
                for (int row = 0; row < grid.length; row++)
                {
                    for (int col = 0; col < grid[0].length; col++)
                    {
                        grid[row][col] = Color.white;
                    }
                }

                elitism();
                yellowPieces.clear();
                yellowPieces = new ArrayList<GAEnemy>(tYellowPop);
                tYellowPop.clear();
                yellowFittest.clear();

                redPieces.clear();
                redPieces = new ArrayList<GAEnemy>(tRedPop);
                tRedPop.clear();
                redFittest.clear();
            }

        }

        @SuppressWarnings("ForLoopReplaceableByForEach")
        public void avgFitness()
        {
            float yAvgFit = 0;
            for(int i = 0; i < yellowPieces.size(); i++)
                yAvgFit += yellowPieces.get(i).fitness;
            yellowAvgFit = (yAvgFit / yellowPieces.size());

            float rAvgFit = 0;
            for(int i = 0; i < redPieces.size(); i++)
                rAvgFit += redPieces.get(i).fitness;
            redAvgFit = (rAvgFit / redPieces.size());
        }

        @SuppressWarnings("unchecked")
        public void elitism()
        {
            yellowFittest = (ArrayList<GAEnemy>)yellowPieces.clone();
            //Sorting the most fit for the yellow GA
            for(int i = 0; i < yellowFittest.size() - 1; i++)
            {
                for(int j = 0; j < yellowFittest.size() - 1; j++)
                {
                    if(yellowFittest.get(j).fitness < yellowFittest.get(j + 1).fitness)
                    {
                        GAEnemy temp = new GAEnemy();
                        for(int k = 0; k < yellowChromoLength; k++)
                        {
                            temp.AddGenes(yellowFittest.get(j).geneList.get(k));
                        }
                        yellowFittest.set(j, yellowFittest.get(j + 1));
                        yellowFittest.set(j + 1, temp);
                    }
                }
            }
            //Remove the most unfit for the yellow GA
            for(int i = 0; i < yellowFittest.size(); i++)
            {
                for(int j = 0; j < yellowFittest.size(); j++)
                {
                    if(j >= yellowElitismAmount - 1)
                    {
                        yellowFittest.remove(j);
                    }
                }
            }

            //Sorting the most fit for the red GA
            redFittest = (ArrayList<GAEnemy>)redPieces.clone();
            for(int i = 0; i < redFittest.size() - 1; i++)
            {
                for(int j = 0; j < redFittest.size() - 1; j++)
                {
                    if(redFittest.get(j).fitness < redFittest.get(j + 1).fitness)
                    {
                        GAEnemy temp = new GAEnemy();
                        for(int k = 0; k < redChromoLength; k++)
                        {
                            temp.AddGenes(redFittest.get(j).geneList.get(k));
                        }
                        redFittest.set(j, redFittest.get(j + 1));
                        redFittest.set(j + 1, temp);
                    }
                }
            }
            //Remove the most unfit for the red GA
            for(int i = 0; i < redFittest.size(); i++)
            {
                for(int j = 0; j < redFittest.size(); j++)
                {
                    if(j >= redElitismAmount - 1)
                    {
                        redFittest.remove(j);
                    }
                }
            }
            selectParents();
        }

        public void selectParents()
        {
            tYellowPop.addAll(yellowFittest);
            for (int i = 0; i < (yellowPopSize - yellowElitismAmount) / 2; i++)
            {
                int momPos = random.nextInt(yellowFittest.size() - 1);
                int dadPos = random.nextInt(yellowFittest.size() - 1);
                GAEnemy mom = yellowFittest.get(momPos);
                GAEnemy dad = yellowFittest.get(dadPos);
                if(momPos != dadPos)
                {
                    //crossover
                    crossover(mom, dad);
                }
                else
                {
                    i--;
                }
            }

            tRedPop.addAll(redFittest);
            for (int i = 0; i < (redPopSize - redElitismAmount) / 2; i++)
            {
                int momPos = random.nextInt(redFittest.size() - 1);
                int dadPos = random.nextInt(redFittest.size() - 1);
                GAEnemy mom = redFittest.get(momPos);
                GAEnemy dad = redFittest.get(dadPos);
                if(momPos != dadPos)
                {
                    //crossover
                    crossover(mom, dad);
                }
                else
                {
                    i--;
                }
            }
        }

        private void crossover(GAEnemy mom, GAEnemy dad)
        {
            GAEnemy baby1 = new GAEnemy();
            GAEnemy baby2 = new GAEnemy();

            //Yellow Crossover
            int crossVal = random.nextInt(yellowChromoLength);

            baby1.geneList = mom.geneList;
            baby2.geneList = dad.geneList;

            for(int i = crossVal; i < baby1.geneList.size(); i++)
            {
                Integer temp = baby1.geneList.get(i);
                baby1.geneList.set(i, baby2.geneList.get(i));
                baby2.geneList.set(i, temp);
            }

            if(random.nextInt(100) == 1)
            {
                //mutation
                mutate(baby1);
                mutate(baby2);
            }

            tYellowPop.add(baby1);
            tYellowPop.add(baby2);

            crossVal = random.nextInt(redChromoLength);

            baby1.geneList = mom.geneList;
            baby2.geneList = dad.geneList;

            for(int i = crossVal; i < baby1.geneList.size(); i++)
            {
                Integer temp = baby1.geneList.get(i);
                baby1.geneList.set(i, baby2.geneList.get(i));
                baby2.geneList.set(i, temp);
            }

            if(random.nextInt(100) == 1)
            {
                //mutation
                mutate(baby1);
                mutate(baby2);
            }

            tRedPop.add(baby1);
            tRedPop.add(baby2);
        }

        private void mutate(GAEnemy piece)
        {
            piece.geneList.set(random.nextInt(piece.geneList.size()), newGene());
            piece.geneList.set(random.nextInt(piece.geneList.size()), newGene());
        }

        public void updateGA()
        {
            if(!redTurn)
            {
                yellowPieces.get(popNum).update();
            }
            else
            {
                redPieces.get(popNum).update();
            }
        }
        //endregion

        //region Game Functions
        /**
         * This is used to get the lowest position from a GA's pick of column.
         * @param clickedCol The column the GA has selected to drop.
         * @param redTurn Whether it is red's turn
         * @return The lowest row that the GA can place its piece
         */
        public int findLowestWhite(int clickedCol, boolean redTurn)
        {
            int clickedRow = 0;

            if(checkIfBoardFull())
            {
                resetGame();
            }

            if(!grid[clickedRow][clickedCol].equals(Color.white))
            {
                yellowPieces.get(popNum).currentGene = newGene();
                return findLowestWhite(yellowPieces.get(popNum).currentGene, redTurn);
            }

            //Starts at top, and the column the gene is at, will drop rows till it finds a white grid cell.
            while(clickedRow <= grid.length - 1)
            {
                //Checks current grid cell for yellow if it's not red's turn.
                if(grid[clickedRow][yellowPieces.get(popNum).currentGene].equals(Color.white) && !redTurn)
                {
                    if(clickedRow == grid.length - 1)
                        return clickedRow;
                    //Check the grid cell below the current one to ensure it is the lowest white cell.
                    else if(grid[clickedRow+1][yellowPieces.get(popNum).currentGene] != Color.white)
                    {
                        return clickedRow;
                    }
                }

                //Checks current grid cell for red if it is red's turn.
                else if(grid[clickedRow][redPieces.get(popNum).currentGene].equals(Color.white) && redTurn)
                {
                    if(clickedRow == grid.length - 1)
                        return clickedRow;
                    //Check the grid cell below the current one to ensure it is the lowest white cell.
                    else if(grid[clickedRow+1][redPieces.get(popNum).currentGene] != Color.white)
                    {
                        return clickedRow;
                    }
                }
                clickedRow++;
            }
            if(!redTurn)
                yellowPieces.get(popNum).currentGene = newGene();
            else
                redPieces.get(popNum).currentGene = newGene();

            return findLowestWhite(clickedCol, redTurn);
        }

        private boolean checkIfBoardFull()
        {
            boolean[] fullTopRow = {
                    grid[0][0] != Color.white,
                    grid[0][1] != Color.white,
                    grid[0][2] != Color.white,
                    grid[0][3] != Color.white,
                    grid[0][4] != Color.white,
                    grid[0][5] != Color.white,
                    grid[0][6] != Color.white
            };

            for(boolean b : fullTopRow) if(!b) return false;
            return true;
        }

        public void resetGame()
        {
            winner=false;
            //Since we are not starting a new generation in resetGame, ensure red goes next if they started first.
            redTurn = redStarted;
            for (int row = 0; row < grid.length; row++)
            {
                for (int col = 0; col < grid[0].length; col++)
                {
                    grid[row][col] = Color.white;
                }
            }
        }
        //endregion

        //region Mouse Events
        public void mousePressed(MouseEvent e)
        {
            gameSpeed = 0;
        }

        public void mouseReleased(MouseEvent e)
        {
            gameSpeed = 100;
        }

        public void mouseEntered(MouseEvent e){}

        public void mouseExited(MouseEvent e){}

        public void mouseClicked(MouseEvent e){}
        //endregion

        //region Win Check
        public boolean isWinner(int clickedColumn,int clickedRow, Color c)
        {
            int x = clickedColumn;
            int count = 1;
            //check left
            x--;
            while(x>=0)
            {
                if(grid[clickedRow][x].equals(c))
                {
                    count++;
                    if(c.equals(Color.yellow))
                    {
                        if(count==2)
                            yellowPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            yellowPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            yellowPieces.get(popNum).fitness = 1f;
                    }
                    if(c.equals(Color.red))
                    {
                        if(count==2)
                            redPieces.get(popNum).fitness = 0.33f;
                        else if(count==3)
                            redPieces.get(popNum).fitness = 0.66f;
                        else if(count==4)
                            redPieces.get(popNum).fitness = 1f;
                    }
                }
                else
                    break;
                if(count==4) return true;
                x--;
            }
            //check right
            x = clickedColumn;
            x++;
            while(x<grid[0].length)
            {

                if(grid[clickedRow][x].equals(c))
                {

                    count++;
                    if(c.equals(Color.yellow))
                    {
                        if(count==2)
                            yellowPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            yellowPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            yellowPieces.get(popNum).fitness = 1f;
                    }
                    if(c.equals(Color.red))
                    {
                        if(count==2)
                            redPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            redPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            redPieces.get(popNum).fitness = 1f;
                    }
                }
                else
                    break;
                if(count==4) return true;
                x++;
            }

            //check up
            count = 1;
            int y = clickedRow;
            y--;
            while(y>0)
            {
                if(grid[y][clickedColumn].equals(c))
                {
                    count++;
                    if(c.equals(Color.yellow))
                    {
                        if(count==2)
                            yellowPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            yellowPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            yellowPieces.get(popNum).fitness = 1f;
                    }
                    if(c.equals(Color.red))
                    {
                        if(count==2)
                            redPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            redPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            redPieces.get(popNum).fitness = 1f;
                    }
                }
                else
                    break;
                if(count==4) return true;
                y--;
            }

            //check down
            y = clickedRow;
            y++;
            while(y<grid.length)
            {
                if(grid[y][clickedColumn].equals(c))
                {
                    count++;
                    if(c.equals(Color.yellow))
                    {
                        if(count==2)
                            yellowPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            yellowPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            yellowPieces.get(popNum).fitness = 1f;
                    }
                    if(c.equals(Color.red))
                    {
                        if(count==2)
                            redPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            redPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            redPieces.get(popNum).fitness = 1f;
                    }
                }
                else
                    break;
                if(count==4) return true;
                y++;
            }

            //check up-left
            count = 1;
            y = clickedRow;
            x = clickedColumn;
            x--;
            y--;
            while(y>0 && x>0)
            {
                if(grid[y][x].equals(c))
                {
                    count++;
                    if(c.equals(Color.yellow))
                    {
                        if(count==2)
                            yellowPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            yellowPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            yellowPieces.get(popNum).fitness = 1f;
                    }
                    if(c.equals(Color.red))
                    {
                        if(count==2)
                            redPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            redPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            redPieces.get(popNum).fitness = 1f;
                    }
                }
                else
                    break;
                if(count==4) return true;
                y--;
                x--;
            }

            //check down-right
            y = clickedRow;
            y++;
            x = clickedColumn;
            x++;
            while(y<grid.length && x<grid.length)
            {
                if(grid[y][x].equals(c))
                {
                    count++;
                    if(c.equals(Color.yellow))
                    {
                        if(count==2)
                            yellowPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            yellowPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            yellowPieces.get(popNum).fitness = 1f;
                    }
                    if(c.equals(Color.red))
                    {
                        if(count==2)
                            redPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            redPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            redPieces.get(popNum).fitness = 1f;
                    }
                }
                else
                    break;
                if(count==4) return true;
                y++;
                x++;
            }

            //check down-left
            count = 1;
            y = clickedRow;
            x = clickedColumn;
            x--;
            y++;
            while(y<grid.length && x>0)
            {
                if(grid[y][x].equals(c))
                {
                    count++;
                    if(c.equals(Color.yellow))
                    {
                        if(count==2)
                            yellowPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            yellowPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            yellowPieces.get(popNum).fitness = 1f;
                    }
                    if(c.equals(Color.red))
                    {
                        if(count==2)
                            redPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            redPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            redPieces.get(popNum).fitness = 1f;
                    }
                }
                else
                    break;
                if(count==4) return true;
                y++;
                x--;
            }

            //check up-right
            y = clickedRow;
            y--;
            x = clickedColumn;
            x++;
            while(y>0 && x<grid.length)
            {
                if(grid[y][x].equals(c))
                {
                    count++;
                    if(c.equals(Color.yellow))
                    {
                        if(count==2)
                            yellowPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            yellowPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            yellowPieces.get(popNum).fitness = 1f;
                    }
                    if(c.equals(Color.red))
                    {
                        if(count==2)
                            redPieces.get(popNum).fitness = 0.33f;
                        if(count==3)
                            redPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
                            redPieces.get(popNum).fitness = 1f;
                    }
                }
                else
                    break;
                if(count==4) return true;
                y--;
                x++;
            }
            return false;
        }
        //endregion
    }
}
