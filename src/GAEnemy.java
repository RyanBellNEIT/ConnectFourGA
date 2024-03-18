import java.util.ArrayList;

public class GAEnemy
{
    public int x;
    public int y;

    public int temp = -1;
    public int currentGene;
    public float fitness;

    public ArrayList<Integer> geneList = new ArrayList<Integer>();

    public GAEnemy()
    {

    }

    public void AddGenes(int tGene)
    {
        geneList.add(tGene);
    }

    public void update()
    {
        if(temp < geneList.size() - 1)
        {
            temp++;
            currentGene = geneList.get(temp);
        }
    }
}

