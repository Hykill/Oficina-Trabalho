package projeto.oficina.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Registro de que um serviço ficou PRONTO.
// Existir uma linha aqui para o serviço = serviço concluído.
// Apagar a linha = reabrir o serviço.
@Entity
@Table (name = "conclusao_servico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConclusaoServicoEntity {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    // @OneToOne + unique: cada serviço só pode ser concluído uma vez
    @OneToOne
    @JoinColumn (name = "servico_id", nullable = false, unique = true)
    private ServicoEntity servico;

    @Column (nullable = false)
    private LocalDateTime dataConclusao;

    @Column
    private String observacao;
}
