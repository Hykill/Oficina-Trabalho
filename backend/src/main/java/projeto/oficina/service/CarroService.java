package projeto.oficina.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import projeto.oficina.dto.CarroDTO;
import projeto.oficina.dto.CarroResponseDTO;
import projeto.oficina.entity.CarroEntity;
import projeto.oficina.entity.ClienteEntity;
import projeto.oficina.mapper.CarroMapper;
import projeto.oficina.repository.CarroRepository;
import projeto.oficina.repository.ClienteRepository;
import projeto.oficina.repository.ServicoRepository;

@Service
@RequiredArgsConstructor
public class CarroService {

    private final CarroRepository carroRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final CarroMapper carroMapper;

    public CarroResponseDTO cadastrar(CarroDTO dto) {
        validarCampos(dto);

        if (carroRepository.existsByPlaca(dto.placa())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um carro com esta placa");
        }

        ClienteEntity cliente = buscarCliente(dto.clienteId());

        CarroEntity carro = carroMapper.paraEntity(dto, cliente);
        CarroEntity salvo = carroRepository.save(carro);
        return carroMapper.paraResponseDTO(salvo);
    }

    public List<CarroResponseDTO> listarTodos() {
        return carroRepository.findAll()
                .stream()
                .map(carroMapper::paraResponseDTO)
                .toList();
    }

    public CarroResponseDTO buscarPorId(Long id) {
        CarroEntity carro = buscarEntityPorId(id);
        return carroMapper.paraResponseDTO(carro);
    }

    public CarroResponseDTO atualizar(Long id, CarroDTO dto) {
        CarroEntity carro = buscarEntityPorId(id);
        validarCampos(dto);

        if (carroRepository.existsByPlacaAndIdNot(dto.placa(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe outro carro com esta placa");
        }

        ClienteEntity cliente = buscarCliente(dto.clienteId());

        carroMapper.atualizarEntity(carro, dto, cliente);
        CarroEntity salvo = carroRepository.save(carro);
        return carroMapper.paraResponseDTO(salvo);
    }

    public void excluir(Long id) {
        CarroEntity carro = buscarEntityPorId(id);

        if (servicoRepository.existsByCarroId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível excluir: o carro possui serviços cadastrados");
        }

        carroRepository.delete(carro);
    }

    // ---------- Métodos auxiliares (uso interno) ----------

    private CarroEntity buscarEntityPorId(Long id) {
        return carroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carro não encontrado"));
    }

    // Transforma o Long clienteId (do DTO) no objeto ClienteEntity (da Entity)
    private ClienteEntity buscarCliente(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    private void validarCampos(CarroDTO dto) {
        if (dto.modelo() == null || dto.modelo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O modelo é obrigatório");
        }
        if (dto.marca() == null || dto.marca().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A marca é obrigatória");
        }
        if (dto.placa() == null || dto.placa().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A placa é obrigatória");
        }
        if (dto.clienteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID do cliente é obrigatório");
        }
    }
}
