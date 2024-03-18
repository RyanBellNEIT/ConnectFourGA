import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;

public class DrawGrid {

    public DrawGrid()
    {
        JFrame frame = new JFrame("DrawGrid");
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

    public static class MultiDraw extends JPanel  implements MouseListener {
        //Game variables.
        int startX = 10;
        int startY = 10;
        int cellSize = 60;
        int turn = 2;
        int rows = 6;
        int cols = 7;
        boolean winner=false;
        //Current Color
        String cColor = "";
        //Controls whose turn it is in game
        boolean redTurn = true;
        public ArrayList<GAEnemy> yellowPieces = new ArrayList<>();
        public ArrayList<GAEnemy> redPieces = new ArrayList<>();

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
        public ArrayList<GAEnemy> yellowFittest = new ArrayList<>();
        public ArrayList<GAEnemy> redFittest = new ArrayList<>();

        //Holds population of red and yellow pieces
        public ArrayList<GAEnemy> tYellowPop = new ArrayList<>();
        public ArrayList<GAEnemy> tRedPop = new ArrayList<>();

        public int gameSpeed = 100;

        private int popNum;
        private int currentGen = 1;


        Color[][] grid = new Color[rows][cols];
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
            initializeGA();
        }

        @Override
        public void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D)g;
            Dimension d = getSize();
            g2.setColor(new Color(0, 0, 255));
            g2.fillRect(0,0,d.width,d.height);
            startX = 0;
            startY = 0;

            redTurn = turn % 2 == 0;
            //Yellow going
            if(!redTurn && !winner)
            {
                int clickedRow = 1;
                int clickedCol = yellowPieces.get(popNum).currentGene;

                clickedRow = findLowestWhite(clickedCol);

                grid[clickedRow][clickedCol] = Color.yellow;
                cColor =  "Yellow";

                updateGA();
                turn++;
                redTurn = true;

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

            //Red placing
            if(redTurn && !winner)
            {
                int clickedRow = 1;
                int clickedCol = redPieces.get(popNum).currentGene;

                clickedRow = findLowestWhite(clickedCol);

                grid[clickedRow][clickedCol] = Color.red;
                //grid[clickedRow][clickedCol] = Color.yellow;
                cColor =  "Red";

                updateGA();
                turn++;
                redTurn = true;

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

            for (int row = 0; row < grid.length; row++)
            {
                for (int col = 0; col < grid[0].length; col++)
                {

                    g2.setColor(grid[row][col]);
                    g2.fillRect(startX,startY,cellSize,cellSize);
                    g2.setColor(Color.black);
                    g2.drawRect(startX,startY,cellSize,cellSize);
                    startX += cellSize;
                }
                startY += cellSize;
                startX = 0;
            }

            g2.setColor(Color.white);
            if(!winner)
            {
                if(redTurn)
                {
                    g2.drawString("Red's Turn",450,20);
                }
                else {
                    g2.drawString("Yellow's Turn",450,20);
                }
            }
            else if(winner)
            {
                g2.drawString("WINNER - "+ cColor,450,20);

                resetGame();
            }

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

            //COMMENT THIS OUT TO MAKE GAME PROGRESS WITHOUT A DELAY
            try {
                Thread.sleep(gameSpeed);
            }
            catch(InterruptedException e)
            {
                System.out.println(e.getMessage());
            }
        }

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
                turn=2;
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



        public void avgFitness()
        {
            float yAvgFit = 0;
            for(int i = 0; i < yellowPieces.size(); i++)
            {
                yAvgFit += yellowPieces.get(i).fitness;
            }
            yellowAvgFit = (yAvgFit / yellowPieces.size());


            float rAvgFit = 0;
            for(int i = 0; i < redPieces.size(); i++)
            {
                yAvgFit += redPieces.get(i).fitness;
            }
            redAvgFit = (yAvgFit / redPieces.size());
        }

        public void elitism()
        {
            yellowFittest = (ArrayList<GAEnemy>)yellowPieces.clone();
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
            for(int i = 0; i < yellowFittest.size(); i++)
            {
                for(int j = 0; j < yellowFittest.size(); j++)
                {
                    if(j >= yellowElitismAmount)
                    {
                        yellowFittest.remove(j);
                    }
                }
            }

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
            for(int i = 0; i < redFittest.size(); i++)
            {
                for(int j = 0; j < redFittest.size(); j++)
                {
                    if(j >= redElitismAmount)
                    {
                        redFittest.remove(j);
                    }
                }
            }
            selectParents();
        }

        public void selectParents()
        {
            for (int i = 0; i < (yellowPopSize - yellowElitismAmount) / 2; i++)
            {
                int momPos = random.nextInt(yellowPieces.size() - 1);
                int dadPos = random.nextInt(yellowPieces.size() - 1);
                GAEnemy mom = yellowPieces.get(momPos);
                GAEnemy dad = yellowPieces.get(dadPos);
                if(momPos != dadPos)
                {
                    //crossover
                    yellowCrossover(mom, dad);
                }
                else
                {
                    i--;
                }
            }
            for(int i = 0; i < yellowElitismAmount; i++)
            {
                tYellowPop.add(yellowFittest.get(i));
            }

            for (int i = 0; i < (redPopSize - redElitismAmount) / 2; i++)
            {
                int momPos = random.nextInt(redPieces.size() - 1);
                int dadPos = random.nextInt(redPieces.size() - 1);
                GAEnemy mom = redPieces.get(momPos);
                GAEnemy dad = redPieces.get(dadPos);
                if(momPos != dadPos)
                {
                    //crossover
                    redCrossover(mom, dad);
                }
                else
                {
                    i--;
                }
            }
            for(int i = 0; i < redElitismAmount; i++)
            {
                tRedPop.add(redFittest.get(i));
            }
        }

        public void yellowCrossover(GAEnemy mom, GAEnemy dad)
        {
            GAEnemy baby1 = new GAEnemy();
            GAEnemy baby2 = new GAEnemy();

            int crossVal = random.nextInt(yellowChromoLength);

            for (int i = 0; i < crossVal; i++)
            {
                baby1.geneList.add(mom.geneList.get(i));
                baby2.geneList.add(dad.geneList.get(i));
            }

            for(int i = crossVal; i < yellowChromoLength; i++)
            {
                baby1.geneList.add(dad.geneList.get(i));
                baby2.geneList.add(mom.geneList.get(i));
            }
            tYellowPop.add(baby1);
            tYellowPop.add(baby2);
            if(random.nextInt(100) == 1)
            {
                //mutation
                yellowMutate(baby1);
                yellowMutate(baby2);
            }
        }
        public void redCrossover(GAEnemy mom, GAEnemy dad)
        {
            GAEnemy baby1 = new GAEnemy();
            GAEnemy baby2 = new GAEnemy();

            int crossVal = random.nextInt(redChromoLength);

            for (int i = 0; i < crossVal; i++)
            {
                baby1.geneList.add(mom.geneList.get(i));
                baby2.geneList.add(dad.geneList.get(i));
            }

            for(int i = crossVal; i < redChromoLength; i++)
            {
                baby1.geneList.add(dad.geneList.get(i));
                baby2.geneList.add(mom.geneList.get(i));
            }
            tRedPop.add(baby1);
            tRedPop.add(baby2);
            if(random.nextInt(100) == 1)
            {
                //mutation
                redMutate(baby1);
                redMutate(baby2);
            }
        }

        public void yellowMutate(GAEnemy yellowPiece)
        {
            yellowPiece.geneList.set(random.nextInt(yellowPiece.geneList.
                    size()), newGene());
            yellowPiece.geneList.set(random.nextInt(yellowPiece.geneList.
                    size()), newGene());
        }

        public void redMutate(GAEnemy redPiece)
        {
            redPiece.geneList.set(random.nextInt(redPiece.geneList.
                    size()), newGene());
            redPiece.geneList.set(random.nextInt(redPiece.geneList.
                    size()), newGene());
        }

        public void updateGA()
        {
            if(!redTurn)
            {
                yellowPieces.get(popNum).update();
            }

            if(redTurn)
            {
                redPieces.get(popNum).update();
            }
        }

        public int findLowestWhite(int clickedCol)
        {
            int clickedRow = grid.length-1;

            while(clickedRow>=0)
            {
                if(grid[clickedRow][clickedCol].equals(Color.white))
                {
                    return clickedRow;
                }
                clickedRow--;
                if(clickedRow >= 0)
                {
                    while(!grid[0][yellowPieces.get(popNum).currentGene]
                            .equals(Color.white)
                            && !grid[0][yellowPieces.get(popNum).currentGene]
                            .equals(Color.red))
                    {
                        yellowPieces.get(popNum).currentGene = newGene();
                    }

                    while(!grid[0][redPieces.get(popNum).currentGene]
                            .equals(Color.white)
                            && !grid[0][redPieces.get(popNum).currentGene]
                            .equals(Color.red))
                    {
                        redPieces.get(popNum).currentGene = newGene();
                    }
                }
            }
            return 0;
        }

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

        public boolean  isWinner(int clickedColumn,int clickedRow, Color c)
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
                        if(count==3)
                            redPieces.get(popNum).fitness = 0.66f;
                        if(count==4)
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

        public void resetGame()
        {
            winner=false;
            turn=2;
            for (int row = 0; row < grid.length; row++)
            {
                for (int col = 0; col < grid[0].length; col++)
                {
                    grid[row][col] = Color.white;
                }
            }
        }
    }
}
