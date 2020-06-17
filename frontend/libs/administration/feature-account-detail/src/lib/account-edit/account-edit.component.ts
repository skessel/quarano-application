import { AccountService, AccountDto } from '@qro/administration/domain';
import { AuthService, roles, IRole } from '@qro/auth/api';
import { BadRequestService } from '@qro/shared/ui-error';
import { MatInput } from '@angular/material/input';
import { ActivatedRoute, Router } from '@angular/router';
import { SubSink } from 'subsink';
import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { FormBuilder, FormGroup, FormControl, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { tap, finalize, distinctUntilChanged, debounceTime } from 'rxjs/operators';
import { SnackbarService } from '@qro/shared/util-snackbar';
import {
  ValidationErrorGenerator,
  ArrayValidator,
  PasswordValidator,
  VALIDATION_PATTERNS,
  TrimmedPatternValidator,
  ConfirmValidPasswordMatcher,
} from '@qro/shared/util-forms';

@Component({
  selector: 'qro-account-edit',
  templateUrl: './account-edit.component.html',
  styleUrls: ['./account-edit.component.scss'],
})
export class AccountEditComponent implements OnInit, OnDestroy {
  account: AccountDto;
  private subs = new SubSink();
  formGroup: FormGroup;
  private usernameIsValid = false;
  roles: IRole[] = roles.filter((r) => r.isHealthDepartmentUser);
  loading = false;
  errorGenerator = ValidationErrorGenerator;
  public confirmValidParentMatcher = new ConfirmValidPasswordMatcher();

  constructor(
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private accountService: AccountService,
    private snackbarService: SnackbarService,
    private router: Router,
    private badRequestService: BadRequestService
  ) {}

  ngOnInit() {
    this.subs.add(
      this.route.data.subscribe((data) => {
        this.account = data.account;
        if (!this.isNew) {
          this.usernameIsValid = true;
        }
      })
    );
    this.buildForm();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  @HostListener('window:beforeunload')
  canDeactivate(): Observable<boolean> | boolean {
    return this.formGroup.pristine;
  }

  buildForm() {
    this.formGroup = this.formBuilder.group(
      {
        firstName: new FormControl(this.account.firstName, [
          Validators.required,
          TrimmedPatternValidator.trimmedPattern(VALIDATION_PATTERNS.name),
        ]),
        lastName: new FormControl(this.account.lastName, [
          Validators.required,
          TrimmedPatternValidator.trimmedPattern(VALIDATION_PATTERNS.name),
        ]),
        password: new FormControl({ value: null, disabled: !this.isNew }, [PasswordValidator.secure]),
        passwordConfirm: new FormControl({ value: null, disabled: !this.isNew }, [Validators.required]),
        username: new FormControl(this.account.username, [
          Validators.required,
          () => (this.usernameIsValid ? null : { usernameInvalid: true }),
        ]),
        email: new FormControl(this.account.email, [
          Validators.required,
          TrimmedPatternValidator.trimmedPattern(VALIDATION_PATTERNS.email),
        ]),
        roles: new FormControl(this.account.roles, [ArrayValidator.minLengthArray(1)]),
      },
      {
        validators: [PasswordValidator.mustMatch, PasswordValidator.mustNotIncludeUsername],
      }
    );
    this.formGroup.controls.username.valueChanges
      .pipe(distinctUntilChanged(), debounceTime(1000))
      .subscribe((value) => this.changeUsername(value));
  }

  get isNew(): boolean {
    return this.account?.accountId == null;
  }

  public changeUsername(username: string) {
    if (this.isNew || username !== this.account.username) {
      this.subs.add(
        this.authService
          .checkUsername(username)
          .pipe(
            tap(() => {
              this.formGroup.get('username').disable();
            }),
            finalize(() => {
              this.formGroup.get('username').enable();
            })
          )
          .subscribe(
            (result) => {
              this.usernameIsValid = result;
            },
            () => {
              this.usernameIsValid = false;
            }
          )
      );
    }
  }

  public submitForm(closeAfterSave: boolean) {
    if (this.formGroup.invalid) {
      this.snackbarService.warning('Bitte alle Pflichtfelder ausfüllen.');
      return;
    }
    this.loading = true;
    Object.assign(this.account, this.formGroup.value);

    if (this.isNew) {
      this.createAccount(closeAfterSave);
    } else {
      this.editAccount(closeAfterSave);
    }
  }

  private createAccount(closeAfterSave: boolean) {
    this.accountService
      .createAccount(this.account)
      .subscribe(
        (result) => {
          this.snackbarService.success(
            `Der Account ${this.account.firstName} ${this.account.lastName} wurde erfolgreich angelegt`
          );
          this.account = result;
          if (closeAfterSave) {
            this.router.navigate(['/administration/accounts/account-list']);
          }
          this.buildForm();
        },
        (error) => {
          this.badRequestService.handleBadRequestError(error, this.formGroup);
        }
      )
      .add(() => (this.loading = false));
  }

  private editAccount(closeAfterSave: boolean) {
    this.accountService
      .editAccount(this.account)
      .subscribe(
        (result) => {
          this.snackbarService.success(
            `Die Accountdaten für ${this.account.firstName} ` +
              `${this.account.lastName} wurden erfolgreich aktualisiert`
          );
          this.account = result;
          if (closeAfterSave) {
            this.router.navigate(['/administration/accounts/account-list']);
          }
          this.buildForm();
        },
        (error) => {
          this.badRequestService.handleBadRequestError(error, this.formGroup);
        }
      )
      .add(() => (this.loading = false));
  }

  trimValue(input: MatInput) {
    input.value = input.value?.trim();
  }
}
