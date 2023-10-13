import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author Stephen Adams
 */
public class Yahtzee {
    public static final double VERSION = 202335.000;
    
    // Enumerate the possible boxes - assumes 6 sided dice are used
    public enum Boxes { U1, U2, U3, U4, U5, U6, TK, FK, FH, SS, LS, Y, YB, C  }

    private final int numberOfDie, dieFaces;

    private final Die[] dice;
    
    int turnPhase; // rolled 0 times, 1 time, 2 times


    // to do: refactor out the int index below, use the above enum instead
    // change Integer[] to enumMap< Boxes, Integer>
    private final int THREEKIND=0,FOURKIND=1,FULLHOUSE=2,SMALLSTRAIGHT=3,LARGESTRAIGHT=4,YAHTZEE=5,CHANCE=6;
    private final Integer[] upperScore;
    private final Integer[] lowerScore;
    private int yahtzeeBonuses; // how many yahtzee Bonuses are earned so far?
    
    public Yahtzee( ) {
        numberOfDie = 5;
        dieFaces = 6;
        
        turnPhase = 0;
        
        dice = new Die[ numberOfDie ];
        for( int i = 0; i < numberOfDie; i++ ) dice[i] = new Die( dieFaces );
        
        upperScore = new Integer[6];
        lowerScore = new Integer[7];
        yahtzeeBonuses = 0;
    }
    
    public String showDice() {
        String temp = "";
        for ( Die d : dice ) {
            temp += d.getCurrentFace() + ",";
        }
        return temp;
    }

    @Override
    public String toString(){
        String card = "U{";
        for ( int i=0; i<upperScore.length; i++ ) {
            card += ( i+1 + ":" + (upperScore[i]==null?"0":upperScore[i]) + "," );
        }
        
        if ( getUpperScore() > 63 )
            card += "Bonus:35";
        else
            card += "Bonus:0";

        card += ",SUB:" + getUpperScore() + "},L{";

        card += String.format( "3K:%d,4K:%d,FH:%d,SS:%d,LS:%d,Y:%d,YB:%d,C:%d,SUB:%d}", 
                lowerScore[THREEKIND]==null?0:lowerScore[THREEKIND], 
                lowerScore[FOURKIND] ==null?0:lowerScore[FOURKIND], 
                lowerScore[FULLHOUSE]==null?0:lowerScore[FULLHOUSE], 
                lowerScore[SMALLSTRAIGHT]==null?0:lowerScore[SMALLSTRAIGHT], 
                lowerScore[LARGESTRAIGHT]==null?0:lowerScore[LARGESTRAIGHT], 
                lowerScore[YAHTZEE]==null?0:lowerScore[YAHTZEE], 
                yahtzeeBonuses * 100,
                lowerScore[CHANCE]==null?0:lowerScore[CHANCE],
                getLowerScore() );
        
        card += ",T{Score:"+getGameScore()+"}";
        
        return card;
    }

    public int getUpperScore() {
        int score = 0;
        for ( Integer i : upperScore )
            score += (i == null ? 0 : i);
        
        // apply upper panel score bonus
        if ( score >= 63 ) score += 35;
        
        return score;
    }
    
    public int getLowerScore() {
        int score = 0;
        for ( Integer i : lowerScore )
            score += (i == null ? 0 : i);
        
        score += yahtzeeBonuses * 100;
        
        return score;
    }

    public Map getScores() {
        Map score = new EnumMap<Boxes, Integer>(Boxes.class);
        
        // to do refactor out the integer array
        score.put(Boxes.U1, upperScore[0] );
        score.put(Boxes.U2, upperScore[1] );
        score.put(Boxes.U3, upperScore[2] );
        score.put(Boxes.U4, upperScore[3] );
        score.put(Boxes.U5, upperScore[4] );
        score.put(Boxes.U6, upperScore[5] );

        score.put(Boxes.TK, lowerScore[THREEKIND] );
        score.put(Boxes.FK, lowerScore[FOURKIND] );
        score.put(Boxes.FH, lowerScore[FULLHOUSE] );
        score.put(Boxes.SS, lowerScore[SMALLSTRAIGHT] );
        score.put(Boxes.LS, lowerScore[LARGESTRAIGHT] );
        score.put(Boxes.Y , lowerScore[YAHTZEE] );
        score.put( Boxes.YB, yahtzeeBonuses);
        score.put(Boxes.C , lowerScore[CHANCE] );
        
        return score;
    }

    public Map getScoreState() {
        Map state = new EnumMap<Boxes, Integer>(Boxes.class);
        
        for ( Boxes b : Boxes.values() ) {
            switch( b ) {
                case U1: if ( upperScore[0] != null ) state.put( b, true ); else state.put( b, false ); break;
                case U2: if ( upperScore[1] != null ) state.put( b, true ); else state.put( b, false ); break;
                case U3: if ( upperScore[2] != null ) state.put( b, true ); else state.put( b, false ); break;
                case U4: if ( upperScore[3] != null ) state.put( b, true ); else state.put( b, false ); break;
                case U5: if ( upperScore[4] != null ) state.put( b, true ); else state.put( b, false ); break;
                case U6: if ( upperScore[5] != null ) state.put( b, true ); else state.put( b, false ); break;
                case TK: if ( lowerScore[THREEKIND] != null ) state.put( b, true ); else state.put( b, false ); break;
                case FK: if ( lowerScore[FOURKIND] != null ) state.put( b, true ); else state.put( b, false ); break;
                case FH: if ( lowerScore[FULLHOUSE] != null ) state.put( b, true ); else state.put( b, false ); break;
                case SS: if ( lowerScore[SMALLSTRAIGHT] != null ) state.put( b, true ); else state.put( b, false ); break;
                case LS: if ( lowerScore[LARGESTRAIGHT] != null ) state.put( b, true ); else state.put( b, false ); break;
                case Y : if ( lowerScore[YAHTZEE] != null ) state.put( b, true ); else state.put( b, false ); break;
                case C : if ( lowerScore[CHANCE] != null ) state.put( b, true ); else state.put( b, false ); break;
            }
        }
        return state;
    }
    
    public int getGameScore() {  return getUpperScore() + getLowerScore(); }

    public Map has( ) {
        // evaluate the current dice set
        int[] roll = new int[5];
        for ( int i=0; i < 5; i++ )
            roll[i] = dice[i].getCurrentFace();
        Arrays.sort( roll );
        
        Map<Boxes, Boolean> boxes = new EnumMap<Boxes, Boolean>(Boxes.class);
        
        // for each thing in the enum check if the roll has it
        for ( Boxes b : Boxes.values() ) {
            boxes.put( b, has( roll, b ) );
        }
        
        return boxes;
    }

    /**
     * @param theRoll an array containing the values of 5 dice, sorted numerically
     * @param b the box you wish to test against
     * @return true if the dice score > 0 in the box you provided
     */
    private boolean has( int[] theRoll, Boxes b ) {
        boolean found = false;
        
        int[] roll = theRoll.clone();
        Arrays.sort(roll);
        
        switch ( b ) {
            case U1: found = contains( 1, roll);break;
            case U2: found = contains( 2, roll);break;
            case U3: found = contains( 3, roll);break;
            case U4: found = contains( 4, roll);break;
            case U5: found = contains( 5, roll);break;
            case U6: found = contains( 6, roll);break;
            case TK:
                found = (roll[0]==roll[1] && roll[0]==roll[2]) || 
                        (roll[1]==roll[2] && roll[1]==roll[3]) ||
                        (roll[2]==roll[3] && roll[2]==roll[4]);
                break;
            case FK:
                found = ( roll[0]==roll[3] || roll[1]==roll[4] );
                break;
            case FH:
                found = ( roll[0] == roll[1] && roll[1] != roll[2] && 
                          roll[2] == roll[3] && roll[3] == roll[4] ) || 
                        ( roll[0] == roll[1] && roll[1] == roll[2] && 
                          roll[2] != roll[3] && roll[3] == roll[4] );
                break;
            case SS:
               // This could be generalized more to check if size = a.length etc
               // 1-2-3-4-* 2-3-4-5-*, *-2-3-4-5, *-3-4-5-6
               // Careful, the * could appear anywhere and throw our indexing right out of whack
               boolean pair = false;
               int inARow = 1;
               for (int i=0; i < roll.length-1; i++) {
                    if ( roll[i]+1 == roll[i+1] )
                        inARow++;
                    else if ( roll[i] == roll[i+1] && !pair )
                        pair = true; // a pair is allowed, once
                    else if ( roll[i] == roll[i+1] && pair )
                        break; // has 3 of a kind or better
                    else
                        inARow = 1;  // if we have any break in the sequence we have to reset

                    if ( inARow == 4 ) break;
                }
                found = inARow == 4;       
                break;
            case LS:
                found = ( roll[0] == 1 && roll[1] == 2 && roll[2] == 3 && roll[3] == 4 && roll[4] == 5 ) || 
                        ( roll[0] == 2 && roll[1] == 3 && roll[2] == 4 && roll[3] == 5 && roll[4] == 6 );
                break;
            case Y:
                found = true;
                for ( int d : roll )
                    found &= ( d == roll[0] );
                break;
            case YB: 
                found = yahtzeeBonuses > 0; break;
            case C: 
                // all yahtzee dice configurations can be scored in chance
                found = true; 
                break; 
        }
        
        return found;
    }

    private boolean contains( int val, int[] roll ) {
        boolean found = false;
        for ( int d : roll ) 
            if ( d == val ) {
                found = true;
                break;
            }
        return found;
    }

    public boolean setScore( Boxes b ) {
        return setScore( b.toString() );
    }

    private boolean setScore( String type ) {
        // removing the old string based version in favor of the enum
        int[] roll = new int[5];
        
        for ( int i = 0; i < 5; i++ )
            roll[i] = dice[i].getCurrentFace();
        
        boolean scoredOK = false;
        int score = 0;

        type = type.toUpperCase();
        
        // all upper scores are Un where n is the value
        // sum all dice whose face is n and score in that box
        if ( type.charAt(0) == 'U' ) {
            int val;
            try {
                val = Integer.parseInt( type.substring(1) );
                if ( val < 1 || val > 6 || upperScore[val-1] != null ) {
                    System.err.println("Upper score position "+val+" is out of range, or value already scored.");
                } else {
                    for ( int d : roll )
                        if ( d == val ) score += val;
                    
                    // if someone wants to score 0 make them scratch
                    if ( score > 0 ) {
                        upperScore[val-1] = score;
                        scoredOK = true;
                    }
                }
            } catch ( NumberFormatException e ) {
                System.err.println("Invalid parameter "+type+" passed to setScore.");
            }
        } else {
            switch( type ) {
            case "TK":
            case "FK":
            case  "C":
                // these are the "sum all dice" pattern
                for (int d : roll) 
                    score += d;
                

                if (type.equals("TK") && has(roll, Boxes.TK) && lowerScore[THREEKIND] == null) {
                    lowerScore[THREEKIND] = score;
                    scoredOK = true;
                } else if (type.equals("FK") && has(roll, Boxes.FK) && lowerScore[FOURKIND] == null) {
                    lowerScore[FOURKIND] = score;
                    scoredOK = true;
                } else if (lowerScore[CHANCE] == null) {
                    lowerScore[CHANCE] = score;
                    scoredOK = true;
                }

                break;
            case "FH":
                if ( has( roll, Boxes.FH ) && lowerScore[FULLHOUSE] == null ) {
                    lowerScore[FULLHOUSE] = 25;
                    scoredOK = true;
                }
                break;
            case "SS":
                if ( has( roll, Boxes.SS ) && lowerScore[SMALLSTRAIGHT] == null ) {
                    lowerScore[SMALLSTRAIGHT] = 30;
                    scoredOK = true;
                }
                break;
            case "LS":
                if ( has( roll, Boxes.LS ) && lowerScore[LARGESTRAIGHT] == null ) {
                    lowerScore[LARGESTRAIGHT] = 40;
                    scoredOK = true;
                }
                break;
            case "Y":
                // If this is the first Yahtzee award 50 points.
                // Each subsequent Yahtzee earns a Yahtzee bonus of 100 points and the
                //      joker scoring comes into effect.
                if ( lowerScore[YAHTZEE] == null ) {
                    lowerScore[YAHTZEE] = 50;
                    scoredOK = true;
                } else {
                    yahtzeeBonuses++;
                    jokerScoring();
                    scoredOK = true;
                }
                break;
            }
        
        }
        
        if ( scoredOK )
            turnPhase = 0;

        return scoredOK;
    }

    private Boxes[] defaultJokerOrder = new Yahtzee.Boxes[]{
                Yahtzee.Boxes.LS, Yahtzee.Boxes.FK, Yahtzee.Boxes.TK,
                Yahtzee.Boxes.SS, Yahtzee.Boxes.FH, Yahtzee.Boxes.C
    };

    private Boxes[] customJokerOrder = null;

    public void setJokerOrder( Boxes[] order ) {
        customJokerOrder = order.clone();
    }

    private void jokerScoring() {
        // We already know that all dice are the same value
        int val = dice[0].getCurrentFace();
        
        // From the official rules: FORCED Joker style

        // Score the total of all 5 dice in the appropriate upper section
        if ( upperScore[ val - 1 ] == null ) {
            // If the upper card spot for the value has not been scored, it must be scored.
            upperScore[ val - 1 ] = 5*val;
        } else {
            boolean scored = false;
            // If the corresponding upper box is filled in you may score in 3K,4K,FH,SS,LS,C as desired

            if ( customJokerOrder != null ) {
                for ( Boxes box : customJokerOrder ) {
                    switch (box) {
                        case TK:
                            if (lowerScore[THREEKIND] == null) {
                                lowerScore[THREEKIND] = 5 * val;
                                scored = true;
                                break;
                            }
                        case FK:
                            if (lowerScore[FOURKIND] == null) {
                                lowerScore[FOURKIND] = 5 * val;
                                scored = true;
                                break;
                            }
                        case FH:
                            if (lowerScore[FULLHOUSE] == null) {
                                lowerScore[FULLHOUSE] = 25;
                                scored = true;
                                break;
                            }
                        case SS:
                            if (lowerScore[SMALLSTRAIGHT] == null) {
                                lowerScore[SMALLSTRAIGHT] = 30;
                                scored = true;
                                break;
                            }
                        case LS:
                            if (lowerScore[LARGESTRAIGHT] == null) {
                                lowerScore[LARGESTRAIGHT] = 40;
                                scored = true;
                                break;
                            }
                        case C:
                            if (lowerScore[CHANCE] == null) {
                                lowerScore[CHANCE] = 5 * val;
                                scored = true;
                                break;
                            }
                    }
                    if (scored) break;
                }
            }

            if ( !scored ) {
                for ( Boxes box : defaultJokerOrder ) {
                    switch (box) {
                        case TK:
                            if (lowerScore[THREEKIND] == null) {
                                lowerScore[THREEKIND] = 5 * val;
                                scored = true;
                            }
                            break;
                        case FK:
                            if (lowerScore[FOURKIND] == null) {
                                lowerScore[FOURKIND] = 5 * val;
                                scored = true;
                            }
                            break;
                        case FH:
                            if (lowerScore[FULLHOUSE] == null) {
                                lowerScore[FULLHOUSE] = 25;
                                scored = true;
                            }
                            break;
                        case SS:
                            if (lowerScore[SMALLSTRAIGHT] == null) {
                                lowerScore[SMALLSTRAIGHT] = 30;
                                scored = true;
                            }
                            break;
                        case LS:
                            if (lowerScore[LARGESTRAIGHT] == null) {
                                lowerScore[LARGESTRAIGHT] = 40;
                                scored = true;
                            }
                            break;
                        case C:
                            if (lowerScore[CHANCE] == null) {
                                lowerScore[CHANCE] = 5 * val;
                                scored = true;
                            }
                            break;
                    }
                    if (scored) break;
                }

                if ( !scored ) {
                    // if the upper card for this value, and the entire lower card is already scored, scratch the lowest upper card value
                    // not configurable since this is both default and optimal already.
                    for (int i = 0; i < 6; i++) {
                        // score in the lowest possible box
                        if (upperScore[i] == null) {
                            upperScore[i] = 0;
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public boolean scratchBox( Boxes b ) {
        boolean scratched = false;
        
        switch ( b ) {
            case U1: if ( upperScore[0] == null ) { upperScore[0] = 0; scratched = true; } break;
            case U2: if ( upperScore[1] == null ) { upperScore[1] = 0; scratched = true; } break;
            case U3: if ( upperScore[2] == null ) { upperScore[2] = 0; scratched = true; } break;
            case U4: if ( upperScore[3] == null ) { upperScore[3] = 0; scratched = true; } break;
            case U5: if ( upperScore[4] == null ) { upperScore[4] = 0; scratched = true; } break;
            case U6: if ( upperScore[5] == null ) { upperScore[5] = 0; scratched = true; } break;
            case TK: if ( lowerScore[THREEKIND] == null ) {lowerScore[THREEKIND] = 0; scratched = true;} break;
            case FK: if ( lowerScore[FOURKIND] == null ) {lowerScore[FOURKIND] = 0; scratched = true;} break;
            case FH: if ( lowerScore[FULLHOUSE] == null ) {lowerScore[FULLHOUSE] = 0; scratched = true;} break;
            case SS: if ( lowerScore[SMALLSTRAIGHT] == null ) {lowerScore[SMALLSTRAIGHT] = 0; scratched = true;} break;
            case LS: if ( lowerScore[LARGESTRAIGHT] == null ) {lowerScore[LARGESTRAIGHT] = 0; scratched = true;} break;
            case Y : if ( lowerScore[YAHTZEE] == null ) {lowerScore[YAHTZEE] = 0; scratched = true;} break;
            case C : if ( lowerScore[CHANCE] == null ) {lowerScore[CHANCE] = 0; scratched = true;} break;
        }
        
        if ( scratched )
            turnPhase = 0;
        
        return scratched;
    }

    public int[] play( boolean[] keep ) {
        int[] currentValues = new int[ numberOfDie ];
        
        switch( turnPhase ) {
            case 0:
                // have not yet rolled the dice
                for ( Die d : dice )
                    d.roll();
                turnPhase++;
                break;
            case 1:
            case 2:
                // rolled the dice once, may keep some and roll others
                for ( int i = 0; i < numberOfDie; i++ ) {
                    if( !keep[i] )
                        dice[i].roll();
                }
                turnPhase++;
                break;
            case 3:
                // rolled the dice twice, may not be rolled further
                // player must score card
                System.err.println( "Reroll not allowed, must score dice." );
                break;
        }
        
        for ( int i = 0; i < numberOfDie; i++ )
            currentValues[i] = dice[i].getCurrentFace();
            
        return currentValues;
    }
    
    public int[] play() {
        boolean[] keep = new boolean[ numberOfDie ];
        return play( keep );
    }

}
