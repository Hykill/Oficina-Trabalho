package projeto.oficina.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import projeto.oficina.dto.ConclusaoServicoDTO;
import projeto.oficina.dto.ConclusaoServicoResponseDTO;
import projeto.oficina.entity.ConclusaoServicoEntity;
import projeto.oficina.entity.ServicoEntity;
import projeto.oficina.mapper.ConclusaoServicoMapper;
import projeto.oficina.repository.ConclusaoServicoRepository;
import projeto.oficina.repository.ServicoRepository;

@Service
@RequiredArgsConstructor
public class ConclusaoServicoService {

    private final ConclusaoServicoRepository conclusaoServicoRepository;
    private final ServicoRepository servicoRepository;
    private final ConclusaoServicoMapper conclusaoServicoMapper;

    // Marca o serviço como pronto
    public ConclusaoServicoResponseDTO cadastrar(ConclusaoServicoDTO dto) {
        validarCampos(dto);

        if (conclusaoServicoRepository.existsByServicoId(dto.servicoId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este serviço já está marcado como pronto");
        }

        ServicoEntity servico = buscarServico(dto.servicoId());

        // Corta as frações de segundo: o MySQL não guarda a mesma precisão do Java
        LocalDateTime agora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        ConclusaoServicoEntity conclusao = conclusaoServicoMapper.paraEntity(dto, servico, agora);
        ConclusaoServicoEntity salva = conclusaoServicoRepository.save(conclusao);
        return conclusaoServicoMapper.paraResponseDTO(salva);
    }

    public List<ConclusaoServicoResponseDTO> listarTodas() {
        return conclusaoServicoRepository.findAll()
                .stream()
                .map(conclusaoServicoMapper::paraResponseDTO)
                .toList();
    }

    public ConclusaoServicoResponseDTO buscarPorId(Long id) {
        ConclusaoServicoEntity conclusao = buscarEntityPorId(id);
        return conclusaoServicoMapper.paraResponseDTO(conclusao);
    }

    public ConclusaoServicoResponseDTO atualizar(Long id, ConclusaoServicoDTO dto) {
        ConclusaoServicoEntity conclusao = buscarEntityPorId(id);
        validarCampos(dto);

        if (conclusaoServicoRepository.existsByServicoIdAndIdNot(dto.servicoId(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este serviço já está marcado como pronto");
        }

        ServicoEntity servico = buscarServico(dto.servicoId());

        conclusaoServicoMapper.atualizarEntity(conclusao, dto, servico);
        ConclusaoServicoEntity salva = conclusaoServicoRepository.save(conclusao);
        return conclusaoServicoMapper.paraResponseDTO(salva);
    }

    // Excluir a conclusão = reabrir o serviço
    public void excluir(Long id) {
        ConclusaoServicoEntity conclusao = buscarEntityPorId(id);
        conclusaoServicoRepository.delete(conclusao);
    }

    // ---------- Métodos auxiliares (uso interno) ----------

    private ConclusaoServicoEntity buscarEntityPorId(Long id) {
        return conclusaoServicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conclusão de serviço não encontrada"));
    }

    // Transforma o Long servicoId (do DTO) no objeto ServicoEntity (da Entity)
    private ServicoEntity buscarServico(Long servicoId) {
        return servicoRepository.findById(servicoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
    }

    private void validarCampos(ConclusaoServicoDTO dto) {
        if (dto.servicoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID do serviço é obrigatório");
        }
    }
}
