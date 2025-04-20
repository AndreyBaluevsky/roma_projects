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
                if(m_curStp.isDone()) m_curStp = null;
                return result;
            }
            return false;
        }
        public boolean doStep() {
            return this.doStep(this.character);
        }

        @Override
        public boolean restart() {
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

    public abstract class BlockWhile extends BlockLinAlg {
        public abstract boolean checkWhile();
        public BlockWhile(GameCharacter character) {
            super(character);
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
