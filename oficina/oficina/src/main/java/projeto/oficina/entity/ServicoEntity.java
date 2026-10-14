package projeto.oficina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity 
@Table (name = "servicos")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class ServicoEntity {
    @Id 
    @GeneratedValue (strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private Double maoDeObra;

    @OneToOne 
    @JoinColumn (name = "item_servico_id")
    private ItemServicoEntity itemServico;

    @ManyToOne 
    @JoinColumn (name = "carro_id")
    private CarroEntity carroId;


}
