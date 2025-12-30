package koschei.models;

import org.springframework.stereotype.Component;

@Component
public class Needle7 {

    private Deth8 deth;

    public Needle7(Deth8 deth8) { this.deth = deth8; }

    @Override
    public String toString() {
        return ", смерть Кощея на игле :( " + deth.toString();
    }
}
