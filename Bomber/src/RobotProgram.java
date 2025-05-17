import java.util.ArrayList;
import java.util.Iterator;

public class RobotProgram {

    public static class алг implements RobotProgramBlock {
        RobotProgramBlock.BlockLinAlg MainBlock;
        public алг(GameCharacter.Movable character) {
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

        public алг кмд(RobotProgramBlock.MotionCommand cmd) {
            MainBlock.put(cmd);
            return this;
        }

        public RobotProgramBlock.BlockWhile нц_пока(Predicate loopCondition) {
            RobotProgramBlock.BlockWhile wh = new BlockWhile(MainBlock.character, loopCondition);
            MainBlock.put(wh);
            return wh;
        }

        @Override
        public boolean isDone() {
            return MainBlock.isDone();
        }
        @Override
        public boolean doStep(GameCharacter.Movable character) {
            return MainBlock.doStep(character);
        }
        @Override
        public boolean restart() {
            return MainBlock.restart();
        }
    }


}
