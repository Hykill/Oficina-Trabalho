package projeto.oficina.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import projeto.oficina.dto.ItemServicoDTO;
import projeto.oficina.dto.ItemServicoResponseDTO;
import projeto.oficina.entity.ItemServicoEntity;
import projeto.oficina.entity.PecaEntity;
import projeto.oficina.entity.ServicoEntity;
import projeto.oficina.mapper.ItemServicoMapper;
import projeto.oficina.repository.ItemServicoRepository;
import projeto.oficina.repository.PecaRepository;
import projeto.oficina.repository.ServicoRepository;

@Service
@RequiredArgsConstructor
public class ItemServicoService {

    private final ItemServicoRepository itemServicoRepository;
    private final ServicoRepository servicoRepository;
    private final PecaRepository pecaRepository;
    private final ItemServicoMapper itemServicoMapper;

    public ItemServicoResponseDTO cadastrar(ItemServicoDTO dto) {
        validarCampos(dto);

        ServicoEntity servico = buscarServico(dto.servicoId());
        PecaEntity peca = buscarPeca(dto.pecaId());

        ItemServicoEntity item = itemServicoMapper.paraEntity(dto, servico, peca);
        ItemServicoEntity salvo = itemServicoRepository.save(item);
        return itemServicoMapper.paraResponseDTO(salvo);
    }

    public List<ItemServicoResponseDTO> listarTodos() {
        return itemServicoRepository.findAll()
                .stream()
                .map(itemServicoMapper::paraResponseDTO)
                .toList();
    }

    public ItemServicoResponseDTO buscarPorId(Long id) {
        ItemServicoEntity item = buscarEntityPorId(id);
        return itemServicoMapper.paraResponseDTO(item);
    }

    public ItemServicoResponseDTO atualizar(Long id, ItemServicoDTO dto) {
        ItemServicoEntity item = buscarEntityPorId(id);
        validarCampos(dto);

        ServicoEntity servico = buscarServico(dto.servicoId());
        PecaEntity peca = buscarPeca(dto.pecaId());

        itemServicoMapper.atualizarEntity(item, dto, servico, peca);
        ItemServicoEntity salvo = itemServicoRepository.save(item);
        return itemServicoMapper.paraResponseDTO(salvo);
    }

    public void excluir(Long id) {
        ItemServicoEntity item = buscarEntityPorId(id);
        itemServicoRepository.delete(item);
    }

    // ---------- Métodos auxiliares (uso interno) ----------

    private ItemServicoEntity buscarEntityPorId(Long id) {
        return itemServicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item de serviço não encontrado"));
    }

    // Transforma o Long servicoId (do DTO) no objeto ServicoEntity (da Entity)
    private ServicoEntity buscarServico(Long servicoId) {
        return servicoRepository.findById(servicoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
    }

    // Transforma o Long pecaId (do DTO) no objeto PecaEntity (da Entity)
    private PecaEntity buscarPeca(Long pecaId) {
        return pecaRepository.findById(pecaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Peça não encontrada"));
    }

    private void validarCampos(ItemServicoDTO dto) {
        if (dto.quantidade() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quantidade é obrigatória");
        }
        if (dto.quantidade() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quantidade deve ser maior que zero");
        }
        if (dto.servicoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID do serviço é obrigatório");
        }
        if (dto.pecaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID da peça é obrigatório");
        }
    }
}
