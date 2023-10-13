/*
* Starter code for the COMP 10185 Yahtzee Strategy Assignment
*
* The code achieves the following results:
*       Iterations: 5000000
*       Min Score: 22
*       Max Score: 793
*       Average Score: 138.28
*       Games>150: 22.59%
*       Games>200: 9.51%
*/
import java.util.Arrays;
import java.util.Map;

public class YahtzeeStrategy {
    // Before performing large numbers of sims, set this to false.
    // Printing to the screen is relatively slow and will cause your game to under perform.
    //final boolean _DEBUG_ = true;
    final boolean _DEBUG_ = false;
    final public static String username = "Najok";

    final Yahtzee.Boxes[] numberBoxes = new Yahtzee.Boxes[]{Yahtzee.Boxes.U1, Yahtzee.Boxes.U2, Yahtzee.Boxes.U3, Yahtzee.Boxes.U4, Yahtzee.Boxes.U5, Yahtzee.Boxes.U6};
    public void debugWrite( String str ) {
        if ( _DEBUG_ )
            System.out.println( str );
    }


    Yahtzee game = new Yahtzee();

    // The basic structure of a turn is that you must roll the dice,
    // choose to score the hand or to reroll some/all of the dice.
    //
    // If scoring you must provide the decision making on what box to score.
    //
    // If rerolling you must provide the decision making on which dice to
    // reroll.
    //
    // You may score or reroll a second time after the first reroll.
    //
    // After the second reroll you must score or scratch (score a 0).


    // Used enumMap instead of boolean[] so I can use enums as indexes.
    // Keep track of which boxes I've already filled.
    Map<Yahtzee.Boxes, Boolean> boxFilled;

    boolean[] keep; // flag array of dice to keep
    int[] roll;  // current turn's dice state

    boolean kept;
    // EXAMPLE GAME PLAY
    // YOU SHOULD HEAVILY EDIT THIS LOGIC TO SUIT YOUR STRATEGY

    // Track what pattern matches are in the roll.
    Map<Yahtzee.Boxes, Boolean> thisRollHas;

    public int play() {
        for (int turnNum = 1; turnNum <= 13; turnNum++) {
            debugWrite( game.toString() );
            debugWrite( "Playing turn " + turnNum + ": ");
            boxFilled = game.getScoreState();
            keep = new boolean[5];
            roll = game.play();
            kept = false;
            int keepingPair = -1;
            // CHANGE THIS STRAGEGY TO SUIT YOURSELF
            // THIS STRATEGY KEEPS YAHTZEES, LARGE STRAIGHTS AND THAT'S IT.
            // YOU SHOULD BE ABLE TO MODULARLIZE THIS WITH SOME THOUGHT

            debugWrite( "Turn " + turnNum + " Roll 1: " + Arrays.toString( roll ) );
            thisRollHas = game.has();

            // does the roll have a yahtzee?
            if (thisRollHas.get(Yahtzee.Boxes.Y)) {
                // note that set score can fail if the pattern doesn't actually match up or
                // if the box is already filled.  Not a problem with yahtzee but maybe for
                // other patterns it is a problem.
                if (game.setScore(Yahtzee.Boxes.Y)) {
                    continue;
                }
            }

            // does the roll have a large straight?
            if (thisRollHas.get(Yahtzee.Boxes.SS) && (!boxFilled.get(Yahtzee.Boxes.SS) || !boxFilled.get(Yahtzee.Boxes.LS))) {
                if (!boxFilled.get(Yahtzee.Boxes.LS)) {
                    if (thisRollHas.get(Yahtzee.Boxes.LS)) {
                        if (game.setScore(Yahtzee.Boxes.LS)) {
                            continue;
                        }
                    } else {
                        int min = -4;
                        kept = true;
                        if (thisRollHas.get(Yahtzee.Boxes.U1) && thisRollHas.get(Yahtzee.Boxes.U2) && thisRollHas.get(Yahtzee.Boxes.U3) && thisRollHas.get(Yahtzee.Boxes.U4)) {
                            min = 1;
                        } else if (thisRollHas.get(Yahtzee.Boxes.U2) && thisRollHas.get(Yahtzee.Boxes.U3) && thisRollHas.get(Yahtzee.Boxes.U4) && thisRollHas.get(Yahtzee.Boxes.U5)) {
                            min = 2;
                        } else {
                            min = 3;
                        }
                        for (int i = min; i < min + 4; i++) {
                            for (int j = 0; j < roll.length; j++) {
                                if (roll[j] == i) {
                                    keep[j] = true;
                                    break;
                                }
                            }
                        }
                    }
                } else if (game.setScore(Yahtzee.Boxes.SS)) {
                    continue;
                }
            }
            // DO NOT SORT THE ROLL ARRAY - the order is significant!!
            // Since it is easier to reason with sorted arrays, we clone the
            // roll and work off a temporary copy.
            int[] tempRoll = roll.clone();
            Arrays.sort(tempRoll);

            // If we have a 3 of a kind or 4 of a kind, roll for yahtzee
            // otherwise roll all 5 dice
            if (thisRollHas.get(Yahtzee.Boxes.FK) || thisRollHas.get(Yahtzee.Boxes.TK)) {
                // if there is a 3 or 4 of a kind, the middle die is always
                // part of the pattern, keep any die that matches it
                if(!boxFilled.get(numberBoxes[tempRoll[2]-1]) || tempRoll[2] >= 3 || thisRollHas.get(Yahtzee.Boxes.FK))
                    for (int i = 0; i < roll.length; i++)
                        if (roll[i] == tempRoll[2]) keep[i] = true;
            }
            // look for a pair
            else if (!kept){
                for (int i = 1; i < tempRoll.length; i++) {
                    if (tempRoll[i] == tempRoll[i-1] && (!boxFilled.get(numberBoxes[tempRoll[i]-1]) || tempRoll[i]>=4) && (!boxFilled.get(Yahtzee.Boxes.TK) || !boxFilled.get(Yahtzee.Boxes.FK))){
                        kept = true;
                        keepingPair = tempRoll[i];
                    }
                }
                for (int i = 0; i < roll.length; i++) {
                    if(roll[i] == keepingPair)
                        keep[i] = true;
                }
            }


            // START ROLL 2
            roll = game.play(keep);
            debugWrite("Turn " + turnNum + " Roll 2: " + Arrays.toString(roll));
            thisRollHas = game.has();
            kept = false;
            keepingPair = -1;
            // NOTE THIS IS THE SAME AS ABOVE, WHICH IS SILLY!!!
            // does the roll have a yahtzee?
            if (thisRollHas.get(Yahtzee.Boxes.Y)) {
                // note that set score can fail if the pattern doesn't actually match up or
                // if the box is already filled.  Not a problem with yahtzee but maybe for
                // other paterns it is a problem.
                if (game.setScore(Yahtzee.Boxes.Y)) {
                    continue;
                }
            }

            // does the roll have a large straight?
            if (thisRollHas.get(Yahtzee.Boxes.SS) && (!boxFilled.get(Yahtzee.Boxes.SS) || !boxFilled.get(Yahtzee.Boxes.LS))) {
                if (!boxFilled.get(Yahtzee.Boxes.LS)) {
                    if (thisRollHas.get(Yahtzee.Boxes.LS)) {
                        if (game.setScore(Yahtzee.Boxes.LS)) {
                            continue;
                        }
                    }
                    else {
                        int min = -4;
                        kept = true;
                        if (thisRollHas.get(Yahtzee.Boxes.U1) && thisRollHas.get(Yahtzee.Boxes.U2) && thisRollHas.get(Yahtzee.Boxes.U3) && thisRollHas.get(Yahtzee.Boxes.U4)) {
                            min = 1;
                        } else if (thisRollHas.get(Yahtzee.Boxes.U2) && thisRollHas.get(Yahtzee.Boxes.U3) && thisRollHas.get(Yahtzee.Boxes.U4) && thisRollHas.get(Yahtzee.Boxes.U5)) {
                            min = 2;
                        } else {
                            min = 3;
                        }
                        for (int i = min; i < min + 4; i++) {
                            for (int j = 0; j < roll.length; j++) {
                                if (roll[j] == i) {
                                    keep[j] = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                else if (game.setScore(Yahtzee.Boxes.SS)) {
                    continue;
                }
            }

            // DO NOT SORT THE ROLL ARRAY - the order is significant!!
            // Since it is easier to reason with sorted arrays, we clone the
            // roll and work off a temporary copy.
            tempRoll = roll.clone();
            Arrays.sort(tempRoll);

            // If we have a 3 of a kind or 4 of a kind, roll for yahtzee
            // otherwise roll all 5 dice
            if (thisRollHas.get(Yahtzee.Boxes.FK) || thisRollHas.get(Yahtzee.Boxes.TK)) {
                // if there is a 3 or 4 of a kind, the middle die is always
                // part of the pattern, keep any die that matches it
                if (thisRollHas.get(Yahtzee.Boxes.FH)) {
                    if (game.setScore(Yahtzee.Boxes.FH))
                        continue;
                }
                if(!boxFilled.get(numberBoxes[tempRoll[2]-1]) || tempRoll[2] >= 3 || thisRollHas.get(Yahtzee.Boxes.FK))
                    for (int i = 0; i < roll.length; i++)
                        if (roll[i] == tempRoll[2]) keep[i] = true;
            }
            else if (!kept){
                for (int i = 1; i < tempRoll.length; i++) {
                    if (tempRoll[i] == tempRoll[i-1] && (!boxFilled.get(numberBoxes[tempRoll[i]-1]) || tempRoll[i]>=4) && (!boxFilled.get(Yahtzee.Boxes.TK) || !boxFilled.get(Yahtzee.Boxes.FK))){
                        keepingPair = tempRoll[i];
                        kept = true;
                    }
                }
                for (int i = 0; i < roll.length; i++) {
                    if(roll[i] == keepingPair)
                        keep[i] = true;
                }
            }

            // START ROLL 3
            roll = game.play(keep);
            debugWrite("Turn " + turnNum + " Roll 3: " + Arrays.toString(roll));
            thisRollHas = game.has();

            // MUST SCORE SOMETHING!!
            if (thisRollHas.get(Yahtzee.Boxes.Y))
                if (game.setScore(Yahtzee.Boxes.Y)) {
                    continue;
                }
            if (thisRollHas.get(Yahtzee.Boxes.SS)) {
                if (thisRollHas.get(Yahtzee.Boxes.LS)) {
                    if (game.setScore(Yahtzee.Boxes.LS)) {
                        continue;
                    }
                }
                if (game.setScore(Yahtzee.Boxes.SS)) {
                    continue;
                }
            }
            //
            tempRoll = roll.clone();
            Arrays.sort(tempRoll);
            if (thisRollHas.get(Yahtzee.Boxes.FK) || thisRollHas.get(Yahtzee.Boxes.TK)) {
                if (thisRollHas.get(Yahtzee.Boxes.FH)) {
                    if (game.setScore(Yahtzee.Boxes.FH))
                        continue;
                }
                switch (tempRoll[2]) {
                    case 1:
                        if (!boxFilled.get(Yahtzee.Boxes.U1)) {
                            game.setScore(Yahtzee.Boxes.U1);
                            continue;
                        }
                        break;
                    case 2:
                        if (!boxFilled.get(Yahtzee.Boxes.U2)) {
                            game.setScore(Yahtzee.Boxes.U2);
                            continue;
                        }
                        break;
                    case 3:
                        if (!boxFilled.get(Yahtzee.Boxes.U3)) {
                            game.setScore(Yahtzee.Boxes.U3);
                            continue;
                        }
                        break;
                    case 4:
                        if (!boxFilled.get(Yahtzee.Boxes.U4)) {
                            game.setScore(Yahtzee.Boxes.U4);
                            continue;
                        }
                        break;
                    case 5:
                        if (!boxFilled.get(Yahtzee.Boxes.U5)) {
                            game.setScore(Yahtzee.Boxes.U5);
                            continue;
                        }
                        break;
                    case 6:
                        if (!boxFilled.get(Yahtzee.Boxes.U6)) {
                            game.setScore(Yahtzee.Boxes.U6);
                            continue;
                        }
                        break;
                }

                if (game.setScore(Yahtzee.Boxes.FK)) continue;
                else if (game.setScore(Yahtzee.Boxes.TK)) continue;
                else if (game.setScore(Yahtzee.Boxes.C)) continue;


            }

            switch (keepingPair){
                case 1:
                    if (game.setScore(Yahtzee.Boxes.U1)) continue;
                    if (game.setScore(Yahtzee.Boxes.C)) continue;
                    break;
                case 2:
                    if (game.setScore(Yahtzee.Boxes.U2)) continue;
                    if (game.setScore(Yahtzee.Boxes.C)) continue;
                    break;
                case 3:
                    if (game.setScore(Yahtzee.Boxes.U3)) continue;
                    if (game.setScore(Yahtzee.Boxes.C)) continue;
                    break;
            }

            // score it anywhere
            boolean scored = false;
            for (Yahtzee.Boxes b : Yahtzee.Boxes.values()) {
                switch (b) {
                    // but I can set priority by rearranging things
                    case C:
                        if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore(Yahtzee.Boxes.C);
                        break;
                    case U1:
                        if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore(Yahtzee.Boxes.U1);
                        break;
                    case U2:
                        if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore(Yahtzee.Boxes.U2);
                        break;
                    case U3:
                        if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore(Yahtzee.Boxes.U3);
                        break;
                    case U4:
                        if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore(Yahtzee.Boxes.U4);
                        break;
                    case U5:
                        if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore(Yahtzee.Boxes.U5);
                        break;
                    case U6:
                        if (!boxFilled.get(b) && thisRollHas.get(b)) scored = game.setScore(Yahtzee.Boxes.U6);
                        break;
                }

                if (scored) {
                    break;
                }
            }

            boolean scratched = false;
            if (!scored) {
                // must scratch, let's do it stupidly
                for (Yahtzee.Boxes b : new Yahtzee.Boxes[]{Yahtzee.Boxes.U1, Yahtzee.Boxes.LS, Yahtzee.Boxes.Y, Yahtzee.Boxes.FK, Yahtzee.Boxes.U2, Yahtzee.Boxes.FH, Yahtzee.Boxes.SS, Yahtzee.Boxes.TK, Yahtzee.Boxes.C, Yahtzee.Boxes.U2, Yahtzee.Boxes.U3, Yahtzee.Boxes.U4, Yahtzee.Boxes.U5, Yahtzee.Boxes.U6}) {
                    scratched = game.scratchBox(b);

                    if (scratched) {
                        break;
                    }
                }
            }

            if (!scored && !scratched)
                System.err.println("Invalid game state, can't score, can't scratch.");

        }
        debugWrite( game.toString() );
        return game.getGameScore() >= 0 ? game.getGameScore() : 0;
    }

}