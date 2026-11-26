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
import projeto.oficina.dto.CarroDTO;
import projeto.oficina.dto.CarroResponseDTO;
import projeto.oficina.service.CarroService;

@RestController
@RequestMapping("/carros")
@RequiredArgsConstructor
public class CarroController {

    private final CarroService carroService;

    @PostMapping
    public ResponseEntity<CarroResponseDTO> cadastrar(@RequestBody CarroDTO dto) {
        CarroResponseDTO criado = carroService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    public ResponseEntity<List<CarroResponseDTO>> listarTodos() {
        return ResponseEntity.ok(carroService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarroResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(carroService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarroResponseDTO> atualizar(@PathVariable Long id, @RequestBody CarroDTO dto) {
        return ResponseEntity.ok(carroService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        carroService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
