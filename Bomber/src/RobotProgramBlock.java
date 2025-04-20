import java.util.ArrayList;
import java.util.Iterator;

public interface RobotProgramBlock {
    boolean isDone();
    boolean doStep(GameCharacter character);
    boolean restart();
    // stuff...
    public interface Command extends RobotProgramBlock {

    }
    public enum MotionCommand implements Command {
        вверх, вниз, влево, вправо;
        private boolean isDone = false;
        @Override public boolean isDone() { return this.isDone; }
        @Override
        public boolean doStep(GameCharacter character) {
            isDone = true;
            switch (this) {
                case вниз:   return character.вниз();
                case вверх:  return character.вверх();
                case влево:  return character.влево();
                case вправо: return character.вправо();
            }
            return false;
        }
        @Override
        public boolean restart() {
            isDone = false;
            return true;
        }
    }

    public interface Predicate {
        boolean eval(GameCharacter character);
    }

    public enum SimplePredicateCommand implements Predicate {
        сверху_свободно,    снизу_свободно,     слева_свободно,     справа_свободно,
        сверху_не_свободно, снизу_не_свободно,  слева_не_свободно,  справа_не_свободно;
        @Override
        public boolean eval(GameCharacter character) {
            switch (this) {
                case сверху_свободно:   return character.сверху_свободно();
                case снизу_свободно:    return character.снизу_свободно();
                case справа_свободно:   return character.справа_свободно();
                case слева_свободно:    return character.слева_свободно();
                case сверху_не_свободно:   return !character.сверху_свободно();
                case снизу_не_свободно:    return !character.снизу_свободно();
                case справа_не_свободно:   return !character.справа_свободно();
                case слева_не_свободно:    return !character.слева_свободно();
            }
            return false;
        }
    }
    public abstract class ComplexPredicate implements Predicate {
        public static Predicate И(SimplePredicateCommand c1, SimplePredicateCommand c2) {
            return new Predicate() {
                @Override
                public boolean eval(GameCharacter character) {
                    return c1.eval(character) && c2.eval(character);
                }
            };
        }
        public static Predicate ИЛИ(SimplePredicateCommand c1, SimplePredicateCommand c2) {
            return new Predicate() {
                @Override
                public boolean eval(GameCharacter character) {
                    return c1.eval(character) || c2.eval(character);
                }
            };
        }
        public static Predicate НЕ(SimplePredicateCommand c) {
            return new Predicate() {
                @Override
                public boolean eval(GameCharacter character) {
                    return !c.eval(character);
                }
            };
        }
    }


    public class BlockLinAlg implements RobotProgramBlock {
        protected GameCharacter character;
        protected RobotProgramBlock m_curStp = null;
        protected ArrayList<RobotProgramBlock> m_steps  = new ArrayList<>();
        protected Iterator<RobotProgramBlock> m_itStep = null;
        protected Iterator<RobotProgramBlock> itStep() {
            if(m_itStep == null) {
                m_itStep = m_steps.iterator();
            }
            return m_itStep;
        }
        public BlockLinAlg(GameCharacter character) {
            this.character = character;
        }
        protected RobotProgramBlock curStp() {
            if( m_curStp == null && itStep().hasNext()) {
                m_curStp = m_itStep.next();
            }
            return m_curStp;
        }
        @Override
        public boolean isDone() {
            return curStp()==null;
        }
        @Override
        public boolean doStep(GameCharacter character) {
            if(!isDone()) {
                boolean result = m_curStp.doStep(character);
                if(result) {
                    if (m_curStp.isDone()) m_curStp = null;
                } else {
                    // шаг просто откладывается до следующей попытки
                    // m_curStp.restart();
                    //return true;
                }
                return result;
            }
            return true; // важно!
        }
        public boolean doStep() {
            return this.doStep(this.character);
        }

        @Override
        public boolean restart() {
            for(RobotProgramBlock s: m_steps) {
                s.restart();
            }
            m_itStep = m_steps.iterator();
            m_curStp = null;
            return true;
        }

        public BlockLinAlg put(RobotProgramBlock ... blocks) {
            for (RobotProgramBlock blk: blocks)
                m_steps.add(blk);
            return this;
        }
    }

    public class BlockWhile extends BlockLinAlg {
        public boolean checkWhile() {
            return loopCondition.eval(character);
        };
        protected Predicate loopCondition;
        public BlockWhile(GameCharacter character, Predicate loopCondition) {
            super(character);
            this.loopCondition = loopCondition;
        }
        @Override
        protected RobotProgramBlock curStp() {
            if(m_curStp == null) {
                if(checkWhile()) {
                    m_itStep = m_steps.iterator();
                } else
                    return null;
            }
            return super.curStp();
        }
    }
}
