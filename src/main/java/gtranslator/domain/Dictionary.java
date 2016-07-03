package gtranslator.domain;

import java.math.BigDecimal;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "dictionary")
public class Dictionary extends Identifier {
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    private Word source;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    private Word target;

    @Column(name = "category")
    private String category;

    @Column
    private BigDecimal weight;

    public Word getSource() {
        return source;
    }

    public void setSource(Word source) {
        this.source = source;
    }

    public Word getTarget() {
        return target;
    }

    public void setTarget(Word target) {
        this.target = target;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
}
