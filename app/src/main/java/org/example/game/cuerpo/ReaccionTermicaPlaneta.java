package org.example.game.cuerpo;

/**
 * Despacho doble polimórfico para interacciones hidro-magmáticas entre planetas (SRP / OCP).
 */
public interface ReaccionTermicaPlaneta {

    boolean interactuar(ReaccionTermicaPlaneta otro);
    boolean reaccionarConAgua();
    boolean reaccionarConLava();

    ReaccionTermicaPlaneta AGUA = new ReaccionTermicaPlaneta() {
        @Override
        public boolean interactuar(ReaccionTermicaPlaneta otro) {
            return otro != null && otro.reaccionarConAgua();
        }

        @Override
        public boolean reaccionarConAgua() {
            return false;
        }

        @Override
        public boolean reaccionarConLava() {
            return true;
        }
    };

    ReaccionTermicaPlaneta LAVA = new ReaccionTermicaPlaneta() {
        @Override
        public boolean interactuar(ReaccionTermicaPlaneta otro) {
            return otro != null && otro.reaccionarConLava();
        }

        @Override
        public boolean reaccionarConAgua() {
            return true;
        }

        @Override
        public boolean reaccionarConLava() {
            return false;
        }
    };

    ReaccionTermicaPlaneta INERTE = new ReaccionTermicaPlaneta() {
        @Override
        public boolean interactuar(ReaccionTermicaPlaneta otro) {
            return false;
        }

        @Override
        public boolean reaccionarConAgua() {
            return false;
        }

        @Override
        public boolean reaccionarConLava() {
            return false;
        }
    };
}
