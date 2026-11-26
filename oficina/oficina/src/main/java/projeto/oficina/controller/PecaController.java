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
import projeto.oficina.dto.PecaDTO;
import projeto.oficina.service.PecaService;

@RestController
@RequestMapping("/pecas")
@RequiredArgsConstructor
public class PecaController {

    private final PecaService pecaService;

    @PostMapping
    public ResponseEntity<PecaDTO> cadastrar(@RequestBody PecaDTO dto) {
        PecaDTO criada = pecaService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<PecaDTO>> listarTodas() {
        return ResponseEntity.ok(pecaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PecaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pecaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PecaDTO> atualizar(@PathVariable Long id, @RequestBody PecaDTO dto) {
        return ResponseEntity.ok(pecaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pecaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
