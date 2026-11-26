package projeto.oficina.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import projeto.oficina.dto.ClienteDTO;
import projeto.oficina.entity.ClienteEntity;
import projeto.oficina.mapper.ClienteMapper;
import projeto.oficina.repository.CarroRepository;
import projeto.oficina.repository.ClienteRepository;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final CarroRepository carroRepository;
    private final ClienteMapper clienteMapper;

    public ClienteDTO cadastrar(ClienteDTO dto) {
        validarCampos(dto);

        if (clienteRepository.existsByCpf(dto.cpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cliente com este CPF");
        }

        ClienteEntity cliente = clienteMapper.paraEntity(dto);
        ClienteEntity salvo = clienteRepository.save(cliente);
        return clienteMapper.paraDTO(salvo);
    }

    public List<ClienteDTO> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(clienteMapper::paraDTO)
                .toList();
    }

    public ClienteDTO buscarPorId(Long id) {
        ClienteEntity cliente = buscarEntityPorId(id);
        return clienteMapper.paraDTO(cliente);
    }

    public ClienteDTO atualizar(Long id, ClienteDTO dto) {
        ClienteEntity cliente = buscarEntityPorId(id);
        validarCampos(dto);

        if (clienteRepository.existsByCpfAndIdNot(dto.cpf(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe outro cliente com este CPF");
        }

        clienteMapper.atualizarEntity(cliente, dto);
        ClienteEntity salvo = clienteRepository.save(cliente);
        return clienteMapper.paraDTO(salvo);
    }

    public void excluir(Long id) {
        ClienteEntity cliente = buscarEntityPorId(id);

        if (carroRepository.existsByClienteId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível excluir: o cliente possui carros cadastrados");
        }

        clienteRepository.delete(cliente);
    }

    // ---------- Métodos auxiliares (uso interno) ----------

    private ClienteEntity buscarEntityPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    private void validarCampos(ClienteDTO dto) {
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome é obrigatório");
        }
        if (dto.cpf() == null || dto.cpf().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O CPF é obrigatório");
        }
    }
}
