package projeto.oficina.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import projeto.oficina.dto.PecaDTO;
import projeto.oficina.entity.PecaEntity;
import projeto.oficina.mapper.PecaMapper;
import projeto.oficina.repository.ItemServicoRepository;
import projeto.oficina.repository.PecaRepository;

@Service
@RequiredArgsConstructor
public class PecaService {

    private final PecaRepository pecaRepository;
    private final ItemServicoRepository itemServicoRepository;
    private final PecaMapper pecaMapper;

    public PecaDTO cadastrar(PecaDTO dto) {
        validarCampos(dto);

        PecaEntity peca = pecaMapper.paraEntity(dto);
        PecaEntity salva = pecaRepository.save(peca);
        return pecaMapper.paraDTO(salva);
    }

    public List<PecaDTO> listarTodas() {
        return pecaRepository.findAll()
                .stream()
                .map(pecaMapper::paraDTO)
                .toList();
    }

    public PecaDTO buscarPorId(Long id) {
        PecaEntity peca = buscarEntityPorId(id);
        return pecaMapper.paraDTO(peca);
    }

    public PecaDTO atualizar(Long id, PecaDTO dto) {
        PecaEntity peca = buscarEntityPorId(id);
        validarCampos(dto);

        pecaMapper.atualizarEntity(peca, dto);
        PecaEntity salva = pecaRepository.save(peca);
        return pecaMapper.paraDTO(salva);
    }

    public void excluir(Long id) {
        PecaEntity peca = buscarEntityPorId(id);

        if (itemServicoRepository.existsByPecaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível excluir: a peça está sendo usada em um serviço");
        }

        pecaRepository.delete(peca);
    }

    // ---------- Métodos auxiliares (uso interno) ----------

    private PecaEntity buscarEntityPorId(Long id) {
        return pecaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Peça não encontrada"));
    }

    private void validarCampos(PecaDTO dto) {
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome é obrigatório");
        }
        if (dto.fabricante() == null || dto.fabricante().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O fabricante é obrigatório");
        }
        if (dto.preco() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O preço é obrigatório");
        }
        if (dto.preco() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O preço não pode ser negativo");
        }
    }
}
