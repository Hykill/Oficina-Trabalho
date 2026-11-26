package projeto.oficina.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import projeto.oficina.dto.ServicoDTO;
import projeto.oficina.dto.ServicoResponseDTO;
import projeto.oficina.entity.CarroEntity;
import projeto.oficina.entity.ServicoEntity;
import projeto.oficina.mapper.ServicoMapper;
import projeto.oficina.repository.CarroRepository;
import projeto.oficina.repository.ItemServicoRepository;
import projeto.oficina.repository.ServicoRepository;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final CarroRepository carroRepository;
    private final ItemServicoRepository itemServicoRepository;
    private final ServicoMapper servicoMapper;

    public ServicoResponseDTO cadastrar(ServicoDTO dto) {
        validarCampos(dto);

        CarroEntity carro = buscarCarro(dto.carroId());

        ServicoEntity servico = servicoMapper.paraEntity(dto, carro);
        ServicoEntity salvo = servicoRepository.save(servico);
        return servicoMapper.paraResponseDTO(salvo);
    }

    public List<ServicoResponseDTO> listarTodos() {
        return servicoRepository.findAll()
                .stream()
                .map(servicoMapper::paraResponseDTO)
                .toList();
    }

    public ServicoResponseDTO buscarPorId(Long id) {
        ServicoEntity servico = buscarEntityPorId(id);
        return servicoMapper.paraResponseDTO(servico);
    }

    public ServicoResponseDTO atualizar(Long id, ServicoDTO dto) {
        ServicoEntity servico = buscarEntityPorId(id);
        validarCampos(dto);

        CarroEntity carro = buscarCarro(dto.carroId());

        servicoMapper.atualizarEntity(servico, dto, carro);
        ServicoEntity salvo = servicoRepository.save(servico);
        return servicoMapper.paraResponseDTO(salvo);
    }

    public void excluir(Long id) {
        ServicoEntity servico = buscarEntityPorId(id);

        if (itemServicoRepository.existsByServicoId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível excluir: o serviço possui itens cadastrados");
        }

        servicoRepository.delete(servico);
    }

    // ---------- Métodos auxiliares (uso interno) ----------

    private ServicoEntity buscarEntityPorId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
    }

    // Transforma o Long carroId (do DTO) no objeto CarroEntity (da Entity)
    private CarroEntity buscarCarro(Long carroId) {
        return carroRepository.findById(carroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carro não encontrado"));
    }

    private void validarCampos(ServicoDTO dto) {
        if (dto.maoDeObra() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor da mão de obra é obrigatório");
        }
        if (dto.maoDeObra() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor da mão de obra não pode ser negativo");
        }
        if (dto.carroId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID do carro é obrigatório");
        }
    }
}
