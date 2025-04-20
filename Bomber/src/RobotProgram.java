import java.util.ArrayList;
import java.util.Iterator;

public class RobotProgram {

    public static class алг implements RobotProgramBlock {
        RobotProgramBlock.BlockLinAlg MainBlock;
        public алг(GameCharacter character) {
            MainBlock = new RobotProgramBlock.BlockLinAlg(character);
        }
        boolean doStep() {
            MainBlock.doStep();
            return false;
        }

        public алг put(RobotProgramBlock.MotionCommand ... cmds) {
            MainBlock.put(cmds);
            return this;
        }

        @Override
        public boolean isDone() {
            return MainBlock.isDone();
        }
        @Override
        public boolean doStep(GameCharacter character) {
            return MainBlock.doStep(character);
        }
        @Override
        public boolean restart() {
            return MainBlock.restart();
        }
    }


}
