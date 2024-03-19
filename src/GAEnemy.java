import java.util.ArrayList;

public class GAEnemy
{
    private int temp = -1;
    public int currentGene;
    public float fitness;

    public ArrayList<Integer> geneList = new ArrayList<Integer>();

    public void AddGenes(int tGene)
    {
        geneList.add(tGene);
    }

    public void update()
    {
        if(temp < geneList.size() - 1)
        {
            currentGene = geneList.get(++temp);
        }
    }
}

