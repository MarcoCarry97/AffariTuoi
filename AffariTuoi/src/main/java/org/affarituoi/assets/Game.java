package org.affarituoi.assets;

import jbook.util.Input;
import org.affarituoi.exceptions.ChoiceException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class Game
{
    public enum Status
    {
        Preparation,
        FirstTurn,
        MiddleTurns,
        FinalTurns,
        Finish,
        Offer
    }

    public enum WinCondition
    {
        Offer,
        Last
    }

    private WinCondition winCondition;

    private Status status;

    private ArrayList<Box> boxes;

    private ArrayList<Integer> bluePrizes;

    private ArrayList<Integer> redPrizes;

    private ArrayList<Integer> openedBoxes;

    private Box playerBox;

    private boolean gameFinished;

    private int offer;

    private boolean boxChangeRequested;

    private float tolerance;

    public Game()
    {
        boxes=new ArrayList<Box>();
        openedBoxes=new ArrayList<Integer>();
        bluePrizes=getBluePrizes();
        redPrizes=getRedPrizes();
        status=Status.Preparation;
        gameFinished=false;
        offer=0;
        boxChangeRequested=false;
        tolerance=20;
        winCondition=WinCondition.Last;
    }

    private ArrayList<Integer> getBluePrizes()
    {
        ArrayList<Integer> prizes=new ArrayList<Integer>();
        prizes.add(0);
        prizes.add(1);
        prizes.add(5);
        prizes.add(10);
        prizes.add(20);
        prizes.add(50);
        prizes.add(75);
        prizes.add(100);
        prizes.add(200);
        prizes.add(500);
        return prizes;
    }

    private ArrayList<Integer> getRedPrizes()
    {
        ArrayList<Integer> prizes=new ArrayList<Integer>();
        prizes.add(5000);
        prizes.add(10000);
        prizes.add(15000);
        prizes.add(20000);
        prizes.add(30000);
        prizes.add(50000);
        prizes.add(75000);
        prizes.add(100000);
        prizes.add(200000);
        prizes.add(300000);
        return prizes;
    }

    public void play()
    {
        while (!gameFinished)
            updateSituation();
    }

    private void updateSituation()
    {
        switch(status)
        {
            case Preparation -> PreparationState();
            case FirstTurn -> FirstTurnState();
            case MiddleTurns -> MiddleTurnsState();
            case FinalTurns -> FinalTurnsState();
            case Finish -> FinishState();
            case Offer -> OfferState();
        }
    }

    private Box getRandomBox()
    {
        int index=getRandomNumber(boxes.size());
        return boxes.remove(index);
    }

    private int getRandomNumber(int num)
    {
        Random rand=new Random();
        return rand.nextInt(num)%num;
    }

    private void PreparationState()
    {
        ArrayList<Integer> prizes=getAllPrizes();
        int number=1;
        while(!prizes.isEmpty())
        {
            int index=getRandomNumber(prizes.size());
            int prize=prizes.remove(index);
            boxes.add(new Box(number++,prize));
        }
        playerBox=getRandomBox();
        status=Status.FirstTurn;
    }

    private void FirstTurnState(){
        doTurn(6);
    }

    private void MiddleTurnsState(){
        doTurn(3);
    }

    private void FinalTurnsState(){
        doTurn(1);
    }

    private void FinishState()
    {
        gameFinished=true;
        int winAmount=0;
        if(winCondition==WinCondition.Last)
        {
            winAmount=playerBox.getValue();
            if(winAmount>=1 && winAmount<50)
                winAmount=50;
        }
        else winAmount=offer;
        System.out.printf("\n\nHai vinto %d €!",winAmount);
    }

    private void OfferState(){
        try
        {
            System.out.println("Driiiin driiiin... è il dottore!");
            int offer=computeOffer();
            float tolerancePercent=(float) tolerance/100;
            int minRange=(int) ((1.0-tolerancePercent)*playerBox.getValue());
            int maxRange=(int) ((1.0+tolerancePercent)*playerBox.getValue());
            if(offer>=minRange && offer<=maxRange && !boxChangeRequested)
            {
                requestToChangeBox();
                boxChangeRequested=true;
            }
            else requestToOfferAPrize(offer);
        } catch (ChoiceException e) {
            System.out.println(e.getMessage());
        }
    }

    private void requestToOfferAPrize(int offer) throws ChoiceException {
        boolean choice=makeDecision(String.format("Il dottore ti offre %d €, accetti? (y/n)",offer));
        if(choice)
        {
            System.out.println("Ringrazio il dottore e accetto l'offerta!\n");
            status=Status.Finish;
            winCondition=WinCondition.Offer;
            this.offer=offer;
        }
        else
        {
            System.out.println("Ringrazio il dottore ma rifiuto e vado avanti!\n");
            changeState();
        }
    }

    private void requestToChangeBox() throws ChoiceException {
        boolean choice=makeDecision("Il dottore ti propone di cambiare pacco, accetti? (y/n)");
        if(choice)
        {
            System.out.println("Scegli il pacco che vuoi tra quelli disponibili\n");
            int chosenBoxId=chooseBox();
            Box box=getBoxFromId(chosenBoxId);
            boxes.remove(box);
            boxes.add(playerBox);
            playerBox=box;
            System.out.printf("Ringrazio il dottore e scelgo di cambiare col pacco %d!\n", chosenBoxId);
        }
        else System.out.println("Ringrazio il dottore ma rifiuto e vado avanti!\n");
        changeState();
    }

    private Box getBoxFromId(int id)
    {
        Box res=null;
        for(Box box : boxes)
            if(box.getId()==id)
                res=box;
        return res;
    }

    public boolean makeDecision(String question) throws ChoiceException {
        char choice=Input.readChar(question);
        if(choice=='y')
            return true;
        else if(choice=='n')
            return false;
        else throw new ChoiceException();
    }

    private void changeState()
    {
        int size=openedBoxes.size();
        if(size>=6 && size<18)
            status=Status.MiddleTurns;
        else if(size==18)
            status=Status.FinalTurns;
        else
        {
            status=Status.Finish;
            winCondition=WinCondition.Last;
        }
    }

    private ArrayList<Integer> getAllPrizes()
    {
        ArrayList<Integer> prizes=new ArrayList<Integer>();
        prizes.addAll(bluePrizes);
        prizes.addAll(redPrizes);
        return prizes;
    }

    private int computeOffer()
    {
        ArrayList<Integer> prizes=getAllPrizes();
        float offer= (float) (3.0/2.0)*playerBox.getValue()/prizes.size();
        for(int prize : prizes)
            if(prize!= playerBox.getValue())
                offer+= (float) prize/prizes.size();
        offer+=1000;
        offer=round(offer);
        return (int) offer;
    }

    private int round(float value)
    {
        float val= value/1000;
        val=Math.round(val);
        int res=(int) val*1000;
        return res;
    }

    private void doTurn(int boxToOpen){
        int i=0;
        while(i<boxToOpen)
        {
            printSituation();
            try
            {
                if(boxToOpen==1)
                {
                    if(!boxChangeRequested)
                        requestToChangeBox();
                    openPlayerBox();
                    Box lastBox=getLastBox();
                    open(lastBox.getId());
                    status=Status.Finish;
                }
                else
                {
                    int choice=chooseBox();
                    open(choice);
                }
                i++;
            }
            catch (ChoiceException e)
            {
                System.out.println(e.getMessage());
            }
        }
        if(openedBoxes.size()!=20)
            status=Status.Offer;
        else status=Status.Finish;
    }

    private Box getLastBox()
    {
        Box res=null;
        for(Box box : boxes)
        {
            if(!openedBoxes.contains(box.getId()))
                res=box;
        }
        return res;
    }

    private int chooseBox() throws ChoiceException {
        int choice= Input.readInt("In base alla situazione, digita il numero di un pacco da aprire\n");
        if(openedBoxes.contains(choice) || choice==playerBox.getId() || choice<1 || choice>20)
            throw new ChoiceException();
        return choice;
    }

    private void printSituation()
    {
        String playerText=String.format("\n\nIl tuo pacco: %d\n",playerBox.getId());
        String otherBoxesText="Pacchi disponibili: ";
        for(Box box : boxes)
            if(!openedBoxes.contains(box.getId()))
                otherBoxesText+=(box.getId()+", ");
        String text=playerText+otherBoxesText+"\n";
        System.out.println(text);
    }

    private void open(int choice)
    {
        for(Box box : boxes)
        {
            if(box.getId()==choice )
            {
                openedBoxes.add(choice);
                if(bluePrizes.contains(box.getValue()))
                    removePrize(bluePrizes,box.getValue());
                else if(redPrizes.contains(box.getValue()))
                    removePrize(redPrizes,box.getValue());
                String text=String.format("Hai aperto il pacco %d e hai trovato %d €!\n",choice,box.getValue());
                System.out.println(text);
            }
        }
    }

    private void openPlayerBox()
    {
        openedBoxes.add(playerBox.getId());
        String text=String.format("Hai aperto il tuo pacco e hai trovato %d €!\n",playerBox.getValue());
        System.out.println(text);
    }

    private void removePrize(ArrayList<Integer> list,int prize)
    {
        boolean end=false;
        for(int i=0;i<list.size() && !end;i++)
        {
            if(list.get(i).equals(prize))
            {
                list.remove(i);
                end=true;
            }
        }
    }
}
