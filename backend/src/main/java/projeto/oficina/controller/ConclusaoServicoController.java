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
import projeto.oficina.dto.ConclusaoServicoDTO;
import projeto.oficina.dto.ConclusaoServicoResponseDTO;
import projeto.oficina.service.ConclusaoServicoService;

@RestController
@RequestMapping("/conclusoes-servico")
@RequiredArgsConstructor
public class ConclusaoServicoController {

    private final ConclusaoServicoService conclusaoServicoService;

    @PostMapping
    public ResponseEntity<ConclusaoServicoResponseDTO> cadastrar(@RequestBody ConclusaoServicoDTO dto) {
        ConclusaoServicoResponseDTO criada = conclusaoServicoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<ConclusaoServicoResponseDTO>> listarTodas() {
        return ResponseEntity.ok(conclusaoServicoService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConclusaoServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(conclusaoServicoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConclusaoServicoResponseDTO> atualizar(@PathVariable Long id, @RequestBody ConclusaoServicoDTO dto) {
        return ResponseEntity.ok(conclusaoServicoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        conclusaoServicoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
