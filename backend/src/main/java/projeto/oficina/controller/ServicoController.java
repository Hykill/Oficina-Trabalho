package projeto.oficina.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import projeto.oficina.dto.ServicoDTO;
import projeto.oficina.dto.ServicoResponseDTO;
import projeto.oficina.service.ServicoService;

@RestController
@RequestMapping("/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoService servicoService;

    @PostMapping
    public ResponseEntity<ServicoResponseDTO> cadastrar(@RequestBody ServicoDTO dto) {
        ServicoResponseDTO criado = servicoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    public ResponseEntity<List<ServicoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(servicoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> atualizar(@PathVariable Long id, @RequestBody ServicoDTO dto) {
        return ResponseEntity.ok(servicoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servicoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
